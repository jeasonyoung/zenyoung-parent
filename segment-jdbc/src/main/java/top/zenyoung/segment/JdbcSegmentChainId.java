package top.zenyoung.segment;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.segment.concurrent.AffinityJob;
import top.zenyoung.segment.concurrent.PrefetchWorker;
import top.zenyoung.segment.concurrent.PrefetchWorkerExecutorService;
import top.zenyoung.segment.distributor.JdbcSegmentDistributor;
import top.zenyoung.segment.exception.NextIdSegmentExpiredException;

import javax.annotation.Nonnull;

/**
 * 分段ID实现
 *
 * @author young
 */
@Slf4j
public class JdbcSegmentChainId implements SegmentIdGenerator {
    public static final int DEFAULT_SAFE_DISTANCE = 10;
    private final long idSegmentTtl;
    private final int safeDistance;
    private final JdbcSegmentDistributor maxIdDistributor;
    private final PrefetchJob prefetchJob;
    @Getter
    private volatile JdbcIdSegmentChain headChain = JdbcIdSegmentChain.newRoot();

    public JdbcSegmentChainId(final long idSegmentTtl, final int safeDistance,
                              @Nonnull final JdbcSegmentDistributor maxIdDistributor,
                              @Nonnull final PrefetchWorkerExecutorService prefetchWorkerExecutorService) {
        Preconditions.checkArgument(idSegmentTtl > 0, Strings.lenientFormat("Illegal idSegmentTtl parameter:[%s].", idSegmentTtl));
        Preconditions.checkArgument(safeDistance > 0, "The safety distance must be greater than 0.");
        this.idSegmentTtl = idSegmentTtl;
        this.safeDistance = safeDistance;
        this.maxIdDistributor = maxIdDistributor;
        this.prefetchJob = new PrefetchJob(this, headChain);
        prefetchWorkerExecutorService.submit(this.prefetchJob);
    }

    private int getSafeDistance() {
        return this.safeDistance > 0 ? this.safeDistance : DEFAULT_SAFE_DISTANCE;
    }

    private void forward(@Nonnull final JdbcIdSegmentChain forwardChain) {
        if (forwardChain.compareTo(headChain) > 0) {
            log.debug("forward - [{}] - [{}] -> [{}].", maxIdDistributor.getNamespace(), headChain, forwardChain);
            headChain = forwardChain;
        }
    }

    private JdbcIdSegmentChain generateNext(@Nonnull final JdbcIdSegmentChain previousChain, final int segments) {
        return maxIdDistributor.nextIdSegmentChain(previousChain, segments, idSegmentTtl);
    }

    @Override
    public long nextId() {
        while (true) {
            JdbcIdSegmentChain chain = headChain;
            while (chain != null) {
                if (chain.isAvailable()) {
                    long nextSeq = chain.incrementAndGet();
                    if (!chain.isOverflow(nextSeq)) {
                        forward(chain);
                        return nextSeq;
                    }
                }
                chain = chain.getNext();
            }
            try {
                final JdbcIdSegmentChain preIdSegmentChain = headChain;
                if (preIdSegmentChain.trySetNext(preChain -> generateNext(preChain, getSafeDistance()))) {
                    final JdbcIdSegmentChain nextChain = preIdSegmentChain.getNext();
                    forward(nextChain);
                    log.debug("generate - [{}] - headChain.version:[{}->{}].",
                            maxIdDistributor.getNamespace(), preIdSegmentChain.getVersion(), nextChain.getVersion());
                }

            } catch (NextIdSegmentExpiredException e) {
                log.warn("generate - [{}] - gave up this next IdSegmentChain.", maxIdDistributor.getNamespace(), e);
            }
            this.prefetchJob.hungry();
        }
    }

    private static class PrefetchJob implements AffinityJob {
        private final ThreadLocal<PrefetchWorker> refPrefetchWorker = ThreadLocal.withInitial(() -> null);
        private static final int MAX_PREFETCH_DISTANCE = 100_000_000;
        private static final long HUNGER_THRESHOLD = 1;
        private volatile long lastHungerTime;
        private final JdbcSegmentChainId segmentChainId;
        private JdbcIdSegmentChain tailChain;
        private int prefetchDistance;

        public PrefetchJob(@Nonnull final JdbcSegmentChainId segmentChainId, @Nonnull final JdbcIdSegmentChain tailChain) {
            this.segmentChainId = segmentChainId;
            this.tailChain = tailChain;
            this.prefetchDistance = segmentChainId.getSafeDistance();
        }

        @Override
        public String getJobId() {
            return this.segmentChainId.maxIdDistributor.getNamespace();
        }

        @Override
        public void setHungerTime(final long hungerTime) {
            lastHungerTime = hungerTime;
        }

        @Override
        public PrefetchWorker getPrefetchWorker() {
            return refPrefetchWorker.get();
        }

        @Override
        public void setPrefetchWorker(@Nonnull final PrefetchWorker worker) {
            if (this.refPrefetchWorker.get() != null) {
                return;
            }
            this.refPrefetchWorker.set(worker);
        }

        @Override
        public void run() {
            prefetch();
        }

        public void prefetch() {
            final long wakeupTimeGap = Clock.CACHE.secondTime() - lastHungerTime;
            final boolean hunger = wakeupTimeGap < HUNGER_THRESHOLD;
            final int prePrefetchDistance = this.prefetchDistance;
            if (hunger) {
                this.prefetchDistance = Math.min(Math.multiplyExact(this.prefetchDistance, 2), MAX_PREFETCH_DISTANCE);
                log.info("prefetch - [{}] - Hunger, Safety distance expansion.[{}->{}]", getJobId(), prePrefetchDistance, this.prefetchDistance);
            } else {
                this.prefetchDistance = Math.max(Math.floorDiv(this.prefetchDistance, 2), this.segmentChainId.getSafeDistance());
                if (prePrefetchDistance > this.prefetchDistance) {
                    log.info("prefetch - [{}] - Full, Safety distance shrinks.[{}->{}]", getJobId(), prePrefetchDistance, this.prefetchDistance);
                }
            }
            JdbcIdSegmentChain availableHeadChain = this.segmentChainId.headChain;
            while (!availableHeadChain.getIdSegment().isAvailable()) {
                availableHeadChain = availableHeadChain.getNext();
                if (availableHeadChain == null) {
                    availableHeadChain = tailChain;
                    break;
                }
            }
            this.segmentChainId.forward(availableHeadChain);
            final int headToTailGap = availableHeadChain.gap(tailChain, this.segmentChainId.maxIdDistributor.getStep());
            final int safeGap = this.segmentChainId.getSafeDistance() - headToTailGap;
            if (safeGap <= 0 && !hunger) {
                log.trace("prefetch - [{}] - safeGap is less than or equal to 0, and is not hungry - headChain.version:[{}] - tailChain.version:[{}].",
                        getJobId(), availableHeadChain.getVersion(), tailChain.getVersion());
                return;
            }
            final int prefetchSegments = hunger ? this.prefetchDistance : safeGap;
            appendChain(availableHeadChain, prefetchSegments);
        }

        private void appendChain(@Nonnull final JdbcIdSegmentChain availableHeadChain, final int prefetchSegments) {
            log.debug("appendChain - [{}] - headChain.version:[{}] - tailChain.version:[{}] - prefetchSegments:[{}].",
                    getJobId(), availableHeadChain.getVersion(), tailChain.getVersion(), prefetchSegments);
            try {
                final JdbcIdSegmentChain preTail = tailChain;
                tailChain = tailChain.ensureSetNext(preChain -> this.segmentChainId.generateNext(preChain, prefetchSegments)).getNext();
                while (tailChain.getNext() != null) {
                    tailChain = tailChain.getNext();
                }
                log.debug("appendChain - [{}] - restTail - tailChain.version:[{}:{}->{}] .",
                        getJobId(),
                        preTail.gap(tailChain, this.segmentChainId.maxIdDistributor.getStep()),
                        preTail.getVersion(), tailChain.getVersion());
            } catch (NextIdSegmentExpiredException e) {
                log.warn("appendChain - [{}] - gave up this next IdSegmentChain.", getJobId(), e);
            }
        }
    }
}
