package top.zenyoung.segment.distributor;

import reactor.core.publisher.Mono;
import top.zenyoung.segment.*;

import javax.annotation.Nonnull;

/**
 * 分布式分段ID
 *
 * @author young
 */
public interface R2dbcSegmentDistributor extends SegmentDistributor {
    /**
     * 获取分段最大值
     *
     * @param step 步长
     * @return 最大值
     */
    Mono<Long> nextMaxId(final long step);

    /**
     * 获取分段最大值
     *
     * @return 最大值
     */
    default Mono<Long> nextMaxId() {
        return nextMaxId(getStep());
    }

    default Mono<R2dbcIdSegment> nextIdSegment(final long ttl) {
        if (ttl <= 0) {
            return Mono.error(new IllegalArgumentException("ttl:[" + ttl + "] must be greater than 0."));
        }
        return nextMaxId()
                .map(maxId -> new R2dbcDefaultIdSegment(maxId, getStep(), Clock.CACHE.secondTime(), ttl));
    }

    default Mono<R2dbcIdSegment> nextIdSegment(final int segments, final long ttl) {
        if (segments <= 0) {
            return Mono.error(new IllegalArgumentException("segments:[" + segments + "] must be greater than 0."));
        }
        if (ttl <= 0) {
            return Mono.error(new IllegalArgumentException("ttl:[" + ttl + "] must be greater than 0."));
        }
        final long totalStep = getStep(segments);
        return nextMaxId(totalStep)
                .map(maxId -> {
                    final R2dbcIdSegment nextIdSegment = new R2dbcDefaultIdSegment(maxId, totalStep, Clock.CACHE.secondTime(), ttl);
                    return new R2dbcMergedIdSegment(segments, nextIdSegment);
                });
    }

    default Mono<R2dbcIdSegmentChain> nextIdSegmentChain(@Nonnull final R2dbcIdSegmentChain previousChain, final int segments, final long ttl) {
        if (DEFAULT_SEGMENTS == segments) {
            return nextIdSegment(ttl)
                    .map(nextIdSegment -> new R2dbcIdSegmentChain(previousChain, nextIdSegment));
        }
        return nextIdSegment(segments, ttl)
                .map(nextIdSegment -> new R2dbcIdSegmentChain(previousChain, nextIdSegment));
    }

    default Mono<Void> ensureStep(final long step) {
        if (step <= 0) {
            return Mono.error(new IllegalArgumentException("step:[" + step + "] must be greater than 0!"));
        }
        return Mono.empty();
    }
}
