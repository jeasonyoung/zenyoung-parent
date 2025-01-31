package top.zenyoung.segment;

import lombok.Getter;
import lombok.ToString;

/**
 * 合并分段ID
 *
 * @author young
 */
@ToString
public class JdbcMergedIdSegment implements JdbcIdSegment {
    @Getter
    private final int segments;
    private final JdbcIdSegment idSegment;
    @Getter
    private final long singleStep;

    public JdbcMergedIdSegment(final int segments, final JdbcIdSegment idSegment) {
        this.segments = segments;
        this.idSegment = idSegment;
        this.singleStep = idSegment.getStep() / segments;
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
