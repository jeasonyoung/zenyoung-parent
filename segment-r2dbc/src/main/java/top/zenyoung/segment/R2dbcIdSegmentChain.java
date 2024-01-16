package top.zenyoung.segment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * 分段ID链路
 *
 * @author young
 */
@Getter
@ToString
@RequiredArgsConstructor
public class R2dbcIdSegmentChain implements R2dbcIdSegment {
    private static final int ROOT_VERSION = -1;
    private static final R2dbcIdSegmentChain NOT_SET = null;
    private final AtomicReference<R2dbcIdSegmentChain> refNext = new AtomicReference<>(null);

    private final long version;
    private final R2dbcIdSegment idSegment;

    public R2dbcIdSegmentChain getNext() {
        return refNext.get();
    }

    public R2dbcIdSegmentChain(@Nonnull final R2dbcIdSegmentChain previousChain, @Nonnull final R2dbcIdSegment idSegment) {
        this(previousChain.getVersion() + 1, idSegment);
    }

    public Mono<Boolean> trySetNext(@Nonnull final Function<R2dbcIdSegmentChain, Mono<R2dbcIdSegmentChain>> chainHandler) {
        if (NOT_SET != getNext()) {
            return Mono.just(false);
        }
        synchronized (this) {
            if (NOT_SET != getNext()) {
                return Mono.just(false);
            }
            return chainHandler.apply(this)
                    .flatMap(next -> setNext(next).map(ret -> true));
        }
    }

    public Mono<Void> setNext(@Nonnull final R2dbcIdSegmentChain nextChain) {
        return ensureNextIdSegment(nextChain)
                .then(Mono.fromSupplier(() -> {
                    this.refNext.set(nextChain);
                    return null;
                }));
    }

    public Mono<R2dbcIdSegmentChain> ensureSetNext(@Nonnull final Function<R2dbcIdSegmentChain, Mono<R2dbcIdSegmentChain>> chainHandler) {
        return ensureSetNext(this, chainHandler);
    }

    private Mono<R2dbcIdSegmentChain> ensureSetNext(@Nullable final R2dbcIdSegmentChain chain,
                                                    @Nonnull final Function<R2dbcIdSegmentChain, Mono<R2dbcIdSegmentChain>> chainHandler) {
        if (Objects.isNull(chain)) {
            return Mono.empty();
        }
        return chain.trySetNext(chainHandler)
                .flatMap(ret -> {
                    if (ret) {
                        return Mono.just(chain);
                    }
                    return ensureSetNext(chain.getNext(), chainHandler);
                });
    }

    public Mono<Integer> gap(@Nonnull final R2dbcIdSegmentChain end, final long step) {
        return end.getMaxId()
                .map(maxId -> (int) ((maxId - getSequence()) / step));
    }

    public static R2dbcIdSegmentChain newRoot() {
        return new R2dbcIdSegmentChain(R2dbcIdSegmentChain.ROOT_VERSION, R2dbcDefaultIdSegment.OVERFLOW);
    }

    @Override
    public long getFetchTime() {
        return idSegment.getFetchTime();
    }

    @Override
    public Mono<Long> getMaxId() {
        return idSegment.getMaxId();
    }

    @Override
    public long getOffset() {
        return idSegment.getOffset();
    }

    @Override
    public long getSequence() {
        return idSegment.getSequence();
    }

    @Override
    public long getStep() {
        return idSegment.getStep();
    }

    @Override
    public Mono<Long> incrementAndGet() {
        return idSegment.incrementAndGet();
    }
}
