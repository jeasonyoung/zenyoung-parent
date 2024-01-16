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
public class JdbcIdSegmentChain implements JdbcIdSegment {
    private static final int ROOT_VERSION = -1;
    private static final JdbcIdSegmentChain NOT_SET = null;
    private final AtomicReference<JdbcIdSegmentChain> refNext = new AtomicReference<>(null);

    private final long version;
    private final JdbcIdSegment idSegment;

    public JdbcIdSegmentChain getNext() {
        return refNext.get();
    }

    public JdbcIdSegmentChain(@Nonnull final JdbcIdSegmentChain previousChain, @Nonnull final JdbcIdSegment idSegment) {
        this(previousChain.getVersion() + 1, idSegment);
    }

    public boolean trySetNext(@Nonnull final UnaryOperator<JdbcIdSegmentChain> idSegmentChainSupplier) throws NextIdSegmentExpiredException {
        if (NOT_SET != getNext()) {
            return false;
        }
        synchronized (this) {
            if (NOT_SET != getNext()) {
                return false;
            }
            final JdbcIdSegmentChain chain = idSegmentChainSupplier.apply(this);
            setNext(chain);
            return true;
        }
    }

    public void setNext(@Nonnull final JdbcIdSegmentChain nextChain) {
        ensureNextIdSegment(nextChain);
        this.refNext.set(nextChain);
    }

    public JdbcIdSegmentChain ensureSetNext(@Nonnull final UnaryOperator<JdbcIdSegmentChain> idSegmentChainSupplier) throws NextIdSegmentExpiredException {
        JdbcIdSegmentChain currentChain = this;
        while (!currentChain.trySetNext(idSegmentChainSupplier)) {
            currentChain = currentChain.getNext();
        }
        return currentChain;
    }

    public int gap(@Nonnull final JdbcIdSegmentChain end, final long step) {
        return (int) ((end.getMaxId() - getSequence()) / step);
    }

    public static JdbcIdSegmentChain newRoot() {
        return new JdbcIdSegmentChain(JdbcIdSegmentChain.ROOT_VERSION, JdbcDefaultIdSegment.OVERFLOW);
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
