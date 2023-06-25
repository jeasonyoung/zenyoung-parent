package top.zenyoung.segment.distributor;

import com.google.common.base.Preconditions;
import top.zenyoung.segment.*;

import javax.annotation.Nonnull;

import static top.zenyoung.segment.IdSegment.TIME_TO_LIVE_FOREVER;

/**
 * 分布式分段ID
 *
 * @author young
 */
public interface IdSegmentDistributor {
    int DEFAULT_SEGMENTS = 1;
    long DEFAULT_OFFSET = 0;
    long DEFAULT_STEP = 100;

    /**
     * 获取命名空间
     *
     * @return 命名空间
     */
    String getNamespace();

    /**
     * 获取步长
     *
     * @return 步长
     */
    long getStep();

    default long getStep(final int segments) {
        return Math.multiplyExact(getStep(), segments);
    }

    long nextMaxId(final long step);

    default long nextMaxId() {
        return nextMaxId(getStep());
    }

    default IdSegment nextIdSegment() {
        return nextIdSegment(TIME_TO_LIVE_FOREVER);
    }

    default IdSegment nextIdSegment(final long ttl) {
        Preconditions.checkArgument(ttl > 0, "ttl:[%s] must be greater than 0.", ttl);

        final long maxId = nextMaxId();
        return new DefaultIdSegment(maxId, getStep(), Clock.CACHE.secondTime(), ttl);
    }

    default IdSegment nextIdSegment(final int segments, final long ttl) {
        Preconditions.checkArgument(segments > 0, "segments:[%s] must be greater than 0.", segments);
        Preconditions.checkArgument(ttl > 0, "ttl:[%s] must be greater than 0.", ttl);

        final long totalStep = getStep(segments);
        final long maxId = nextMaxId(totalStep);
        final IdSegment nextIdSegment = new DefaultIdSegment(maxId, totalStep, Clock.CACHE.secondTime(), ttl);
        return new MergedIdSegment(segments, nextIdSegment);
    }

    default IdSegmentChain nextIdSegmentChain(@Nonnull final IdSegmentChain previousChain) {
        return nextIdSegmentChain(previousChain, DEFAULT_SEGMENTS, TIME_TO_LIVE_FOREVER);
    }

    default IdSegmentChain nextIdSegmentChain(@Nonnull final IdSegmentChain previousChain, final int segments, final long ttl) {
        if (DEFAULT_SEGMENTS == segments) {
            final IdSegment nextIdSegment = nextIdSegment(ttl);
            return new IdSegmentChain(previousChain, nextIdSegment);
        }
        final IdSegment nextIdSegment = nextIdSegment(segments, ttl);
        return new IdSegmentChain(previousChain, nextIdSegment);
    }

    static void ensureStep(final long step) {
        Preconditions.checkArgument(step > 0, "step:[%s] must be greater than 0!", step);
    }
}
