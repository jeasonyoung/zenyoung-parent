package top.zenyoung.segment.distributor;

import com.google.common.base.Preconditions;
import top.zenyoung.segment.*;

import javax.annotation.Nonnull;

/**
 * 分布式分段ID
 *
 * @author young
 */
public interface JdbcSegmentDistributor extends SegmentDistributor {
    long nextMaxId(final long step);

    default long nextMaxId() {
        return nextMaxId(getStep());
    }

    default JdbcIdSegment nextIdSegment(final long ttl) {
        Preconditions.checkArgument(ttl > 0, "ttl:[%s] must be greater than 0.", ttl);

        final long maxId = nextMaxId();
        return new JdbcDefaultIdSegment(maxId, getStep(), Clock.CACHE.secondTime(), ttl);
    }

    default JdbcIdSegment nextIdSegment(final int segments, final long ttl) {
        Preconditions.checkArgument(segments > 0, "segments:[%s] must be greater than 0.", segments);
        Preconditions.checkArgument(ttl > 0, "ttl:[%s] must be greater than 0.", ttl);

        final long totalStep = getStep(segments);
        final long maxId = nextMaxId(totalStep);
        final JdbcIdSegment nextIdSegment = new JdbcDefaultIdSegment(maxId, totalStep, Clock.CACHE.secondTime(), ttl);
        return new JdbcMergedIdSegment(segments, nextIdSegment);
    }

    default JdbcIdSegmentChain nextIdSegmentChain(@Nonnull final JdbcIdSegmentChain previousChain, final int segments, final long ttl) {
        if (DEFAULT_SEGMENTS == segments) {
            final JdbcIdSegment nextIdSegment = nextIdSegment(ttl);
            return new JdbcIdSegmentChain(previousChain, nextIdSegment);
        }
        final JdbcIdSegment nextIdSegment = nextIdSegment(segments, ttl);
        return new JdbcIdSegmentChain(previousChain, nextIdSegment);
    }

    default void ensureStep(final long step) {
        Preconditions.checkArgument(step > 0, "step:[%s] must be greater than 0!", step);
    }
}
