package top.zenyoung.segment;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import top.zenyoung.segment.concurrent.AffinityJob;
import top.zenyoung.segment.concurrent.PrefetchWorker;
import top.zenyoung.segment.concurrent.PrefetchWorkerExecutorService;
import top.zenyoung.segment.distributor.R2dbcSegmentDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 分段ID实现
 *
 * @author young
 */
@Slf4j
public class R2dbcSegmentChainId implements SegmentIdGenerator {
    public static final int DEFAULT_SAFE_DISTANCE = 10;
    private final long idSegmentTtl;
    private final int safeDistance;
    private final R2dbcSegmentDistributor maxIdDistributor;
    private final PrefetchJob prefetchJob;
    private final AtomicReference<R2dbcIdSegmentChain> refHeadChain = new AtomicReference<>(R2dbcIdSegmentChain.newRoot());

    public R2dbcSegmentChainId(final long idSegmentTtl, final int safeDistance,
                               @Nonnull final R2dbcSegmentDistributor maxIdDistributor,
                               @Nonnull final PrefetchWorkerExecutorService prefetchWorkerExecutorService) {
        Preconditions.checkArgument(idSegmentTtl > 0, Strings.lenientFormat("Illegal idSegmentTtl parameter:[%s].", idSegmentTtl));
        Preconditions.checkArgument(safeDistance > 0, "The safety distance must be greater than 0.");
        this.idSegmentTtl = idSegmentTtl;
        this.safeDistance = safeDistance;
        this.maxIdDistributor = maxIdDistributor;
        this.prefetchJob = new PrefetchJob(this);
        prefetchWorkerExecutorService.submit(this.prefetchJob);
    }

    private int getSafeDistance() {
        return this.safeDistance > 0 ? this.safeDistance : DEFAULT_SAFE_DISTANCE;
    }

    private void forward(@Nonnull final R2dbcIdSegmentChain forwardChain) {
        if (forwardChain.compareTo(refHeadChain.get()) > 0) {
            log.debug("forward - [{}] - [{}] -> [{}].", maxIdDistributor.getNamespace(), refHeadChain.get(), forwardChain);
            refHeadChain.set(forwardChain);
        }
    }

    private Mono<R2dbcIdSegmentChain> generateNext(@Nonnull final R2dbcIdSegmentChain previousChain, final int segments) {
        return maxIdDistributor.nextIdSegmentChain(previousChain, segments, idSegmentTtl);
    }

    private Mono<Long> chainNextHandler(@Nullable final R2dbcIdSegmentChain chain) {
        if (Objects.isNull(chain)) {
            return Mono.just(-1L);
        }
        return chain.isAvailable()
                .flatMap(available -> {
                    if (available) {
                        return chain.incrementAndGet()
                                .flatMap(nextSeq -> chain.isOverflow(nextSeq)
                                        .flatMap(overflow -> {
                                            if (overflow) {
                                                return chainNextHandler(chain.getNext());
                                            }
                                            forward(chain);
                                            return Mono.just(nextSeq);
                                        })
                                );
                    }
                    return chainNextHandler(chain.getNext());
                });
    }

    @Override
    public Mono<Long> nextId() {
        return chainNextHandler(refHeadChain.get())
                .flatMap(nextId -> {
                    if (nextId > 0) {
                        return Mono.just(nextId);
                    }
                    final R2dbcIdSegmentChain preChain = refHeadChain.get();
                    return preChain.trySetNext(pc -> generateNext(pc, getSafeDistance()))
                            .flatMap(ret -> {
                                if (ret) {
                                    final R2dbcIdSegmentChain nextChain = preChain.getNext();
                                    forward(nextChain);
                                    log.info("generate - [{}] - headChain.version:[{}->{}].",
                                            maxIdDistributor.getNamespace(), preChain.getVersion(), nextChain.getVersion());
                                }
                                prefetchJob.hungry();
                                return nextId();
                            });
                });
    }

    private static class PrefetchJob implements AffinityJob {
        private final ThreadLocal<PrefetchWorker> refPrefetchWorker = ThreadLocal.withInitial(() -> null);
        private static final int MAX_PREFETCH_DISTANCE = 100_000_000;
        private static final long HUNGER_THRESHOLD = 1;
        private volatile long lastHungerTime;
        private final R2dbcSegmentChainId segmentChainId;
        private int prefetchDistance;

        public PrefetchJob(@Nonnull final R2dbcSegmentChainId segmentChainId) {
            this.segmentChainId = segmentChainId;
            this.prefetchDistance = segmentChainId.getSafeDistance();
        }

        private R2dbcIdSegmentChain getTailChain() {
            return this.segmentChainId.refHeadChain.get();
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
            prefetch().subscribe();
        }

        private Mono<R2dbcIdSegmentChain> availableHeadChainHandler(@Nonnull final R2dbcIdSegmentChain chain) {
            return chain.getIdSegment().isAvailable()
                    .flatMap(available -> {
                        if (available) {
                            return Mono.just(chain);
                        }
                        final R2dbcIdSegmentChain next = chain.getNext();
                        if (Objects.isNull(next)) {
                            return Mono.justOrEmpty(getTailChain());
                        }
                        return availableHeadChainHandler(next);
                    });
        }


        public Mono<Void> prefetch() {
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
            return availableHeadChainHandler(this.segmentChainId.refHeadChain.get())
                    .flatMap(availableHeadChain -> {
                        this.segmentChainId.forward(availableHeadChain);
                        return availableHeadChain.gap(getTailChain(), this.segmentChainId.maxIdDistributor.getStep())
                                .flatMap(headToTailGap -> {
                                    final int safeGap = this.segmentChainId.getSafeDistance() - headToTailGap;
                                    if (safeGap <= 0 && !hunger) {
                                        final R2dbcIdSegmentChain tailChain = getTailChain();
                                        log.info("prefetch - [{}] - safeGap is less than or equal to 0, and is not hungry - headChain.version:[{}] - tailChain.version:[{}].",
                                                getJobId(), availableHeadChain.getVersion(), Objects.nonNull(tailChain) ? tailChain.getVersion() : 0);
                                        return Mono.justOrEmpty((Void) null);
                                    }
                                    final int prefetchSegments = hunger ? this.prefetchDistance : safeGap;
                                    return appendChain(availableHeadChain, prefetchSegments).then();
                                });
                    }).then();
        }

        private Mono<Void> appendChain(@Nonnull final R2dbcIdSegmentChain availableHeadChain, final int prefetchSegments) {
            final R2dbcIdSegmentChain tailChain = getTailChain();
            log.info("appendChain - [{}] - headChain.version:[{}] - tailChain.version:[{}] - prefetchSegments:[{}].",
                    getJobId(), availableHeadChain.getVersion(), tailChain.getVersion(), prefetchSegments);
            return getTailChain().ensureSetNext(pc -> segmentChainId.generateNext(pc, prefetchSegments))
                    .map(chain -> {
                        log.info("appendChain - [{}] - restTail - tailChain.version:[{}:{}->{}] .",
                                getJobId(),
                                tailChain.gap(tailChain, this.segmentChainId.maxIdDistributor.getStep()),
                                tailChain.getVersion(), tailChain.getVersion());
                        return chain;
                    }).then();
        }
    }
}
