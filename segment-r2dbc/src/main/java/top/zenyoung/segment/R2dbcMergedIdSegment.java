package top.zenyoung.segment;

import lombok.Getter;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * 合并分段ID
 *
 * @author young
 */
@ToString
public class R2dbcMergedIdSegment implements R2dbcIdSegment {
    @Getter
    private final int segments;
    private final R2dbcIdSegment idSegment;
    @Getter
    private final long singleStep;

    public R2dbcMergedIdSegment(final int segments, final R2dbcIdSegment idSegment) {
        this.segments = segments;
        this.idSegment = idSegment;
        this.singleStep = idSegment.getStep() / segments;
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
