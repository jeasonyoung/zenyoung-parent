package top.zenyoung.segment;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.segment.concurrent.AffinityJob;
import top.zenyoung.segment.concurrent.PrefetchWorker;
import top.zenyoung.segment.concurrent.PrefetchWorkerExecutorService;
import top.zenyoung.segment.distributor.IdSegmentDistributor;
import top.zenyoung.segment.exception.NextIdSegmentExpiredException;

import javax.annotation.Nonnull;

/**
 * 分段ID实现
 *
 * @author young
 */
@Slf4j
public class SegmentChainId implements SegmentIdGenerator {
    public static final int DEFAULT_SAFE_DISTANCE = 10;
    private final long idSegmentTtl;
    private final int safeDistance;
    private final IdSegmentDistributor maxIdDistributor;
    private final PrefetchJob prefetchJob;
    @Getter
    private volatile IdSegmentChain headChain = IdSegmentChain.newRoot();

    public SegmentChainId(final long idSegmentTtl, final int safeDistance,
                          @Nonnull final IdSegmentDistributor maxIdDistributor,
                          @Nonnull final PrefetchWorkerExecutorService prefetchWorkerExecutorService) {
        Preconditions.checkArgument(idSegmentTtl > 0, Strings.lenientFormat("Illegal idSegmentTtl parameter:[%s].", idSegmentTtl));
        Preconditions.checkArgument(safeDistance > 0, "The safety distance must be greater than 0.");
        this.idSegmentTtl = idSegmentTtl;
        this.safeDistance = safeDistance;
        this.maxIdDistributor = maxIdDistributor;
        this.prefetchJob = new PrefetchJob(headChain);
        prefetchWorkerExecutorService.submit(this.prefetchJob);
    }

    private void forward(@Nonnull final IdSegmentChain forwardChain) {
        if (forwardChain.compareTo(headChain) > 0) {
            log.debug("forward - [{}] - [{}] -> [{}].", maxIdDistributor.getNamespace(), headChain, forwardChain);
            headChain = forwardChain;
        }
    }

    private IdSegmentChain generateNext(@Nonnull final IdSegmentChain previousChain, final int segments) {
        return maxIdDistributor.nextIdSegmentChain(previousChain, segments, idSegmentTtl);
    }

    @Override
    public Long nextId() {
        while (true) {
            IdSegmentChain chain = headChain;
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
                final IdSegmentChain preIdSegmentChain = headChain;
                if (preIdSegmentChain.trySetNext(preChain -> generateNext(preChain, safeDistance))) {
                    final IdSegmentChain nextChain = preIdSegmentChain.getNext();
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

    private class PrefetchJob implements AffinityJob {
        private static final int MAX_PREFETCH_DISTANCE = 100_000_000;
        private static final long HUNGER_THRESHOLD = 1;
        private volatile PrefetchWorker prefetchWorker;
        private int prefetchDistance = safeDistance;
        private IdSegmentChain tailChain;
        private volatile long lastHungerTime;

        public PrefetchJob(@Nonnull final IdSegmentChain tailChain) {
            this.tailChain = tailChain;
        }

        @Override
        public String getJobId() {
            return maxIdDistributor.getNamespace();
        }

        @Override
        public void setHungerTime(final long hungerTime) {
            lastHungerTime = hungerTime;
        }

        @Override
        public PrefetchWorker getPrefetchWorker() {
            return prefetchWorker;
        }

        @Override
        public void setPrefetchWorker(@Nonnull final PrefetchWorker worker) {
            if (this.prefetchWorker != null) {
                return;
            }
            this.prefetchWorker = worker;
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
                log.info("prefetch - [{}] - Hunger, Safety distance expansion.[{}->{}]", maxIdDistributor.getNamespace(), prePrefetchDistance, this.prefetchDistance);
            } else {
                this.prefetchDistance = Math.max(Math.floorDiv(this.prefetchDistance, 2), safeDistance);
                if (prePrefetchDistance > this.prefetchDistance) {
                    log.info("prefetch - [{}] - Full, Safety distance shrinks.[{}->{}]", maxIdDistributor.getNamespace(), prePrefetchDistance, this.prefetchDistance);
                }
            }
            IdSegmentChain availableHeadChain = SegmentChainId.this.headChain;
            while (!availableHeadChain.getIdSegment().isAvailable()) {
                availableHeadChain = availableHeadChain.getNext();
                if (availableHeadChain == null) {
                    availableHeadChain = tailChain;
                    break;
                }
            }
            forward(availableHeadChain);
            final int headToTailGap = availableHeadChain.gap(tailChain, maxIdDistributor.getStep());
            final int safeGap = safeDistance - headToTailGap;
            if (safeGap <= 0 && !hunger) {
                log.trace("prefetch - [{}] - safeGap is less than or equal to 0, and is not hungry - headChain.version:[{}] - tailChain.version:[{}].",
                        maxIdDistributor.getNamespace(), availableHeadChain.getVersion(), tailChain.getVersion());
                return;
            }
            final int prefetchSegments = hunger ? this.prefetchDistance : safeGap;
            appendChain(availableHeadChain, prefetchSegments);
        }

        private void appendChain(@Nonnull final IdSegmentChain availableHeadChain, final int prefetchSegments) {
            log.debug("appendChain - [{}] - headChain.version:[{}] - tailChain.version:[{}] - prefetchSegments:[{}].",
                    maxIdDistributor.getNamespace(), availableHeadChain.getVersion(), tailChain.getVersion(), prefetchSegments);
            try {
                final IdSegmentChain preTail = tailChain;
                tailChain = tailChain.ensureSetNext((preChain) -> generateNext(preChain, prefetchSegments)).getNext();
                while (tailChain.getNext() != null) {
                    tailChain = tailChain.getNext();
                }
                log.debug("appendChain - [{}] - restTail - tailChain.version:[{}:{}->{}] .",
                        maxIdDistributor.getNamespace(), preTail.gap(tailChain, maxIdDistributor.getStep()), preTail.getVersion(), tailChain.getVersion());
            } catch (NextIdSegmentExpiredException e) {
                log.warn("appendChain - [{}] - gave up this next IdSegmentChain.", maxIdDistributor.getNamespace(), e);
            }
        }
    }
}
