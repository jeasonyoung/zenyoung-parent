package top.zenyoung.segment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import top.zenyoung.segment.exception.NextIdSegmentExpiredException;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

/**
 * 分段ID链路
 *
 * @author young
 */
@Getter
@ToString
@RequiredArgsConstructor
public class IdSegmentChain implements IdSegment {
    private static final int ROOT_VERSION = -1;
    private static final IdSegmentChain NOT_SET = null;
    private final AtomicReference<IdSegmentChain> refNext = new AtomicReference<>(null);

    private final long version;
    private final IdSegment idSegment;

    public IdSegmentChain getNext() {
        return refNext.get();
    }

    public IdSegmentChain(@Nonnull final IdSegmentChain previousChain, @Nonnull final IdSegment idSegment) {
        this(previousChain.getVersion() + 1, idSegment);
    }

    public boolean trySetNext(@Nonnull final UnaryOperator<IdSegmentChain> idSegmentChainSupplier) throws NextIdSegmentExpiredException {
        if (NOT_SET != getNext()) {
            return false;
        }
        synchronized (this) {
            if (NOT_SET != getNext()) {
                return false;
            }
            final IdSegmentChain chain = idSegmentChainSupplier.apply(this);
            setNext(chain);
            return true;
        }
    }

    public void setNext(@Nonnull final IdSegmentChain nextChain) {
        ensureNextIdSegment(nextChain);
        this.refNext.set(nextChain);
    }

    public IdSegmentChain ensureSetNext(@Nonnull final UnaryOperator<IdSegmentChain> idSegmentChainSupplier) throws NextIdSegmentExpiredException {
        IdSegmentChain currentChain = this;
        while (!currentChain.trySetNext(idSegmentChainSupplier)) {
            currentChain = currentChain.getNext();
        }
        return currentChain;
    }

    public int gap(@Nonnull final IdSegmentChain end, final long step) {
        return (int) ((end.getMaxId() - getSequence()) / step);
    }

    public static IdSegmentChain newRoot() {
        return new IdSegmentChain(IdSegmentChain.ROOT_VERSION, DefaultIdSegment.OVERFLOW);
    }

    @Override
    public long getFetchTime() {
        return idSegment.getFetchTime();
    }

    @Override
    public long getMaxId() {
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
    public long incrementAndGet() {
        return idSegment.incrementAndGet();
    }
}
