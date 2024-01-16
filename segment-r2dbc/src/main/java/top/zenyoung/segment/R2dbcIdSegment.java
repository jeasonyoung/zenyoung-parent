package top.zenyoung.segment;

import reactor.core.publisher.Mono;
import top.zenyoung.segment.exception.NextIdSegmentExpiredException;

import javax.annotation.Nonnull;

/**
 * ID分段
 *
 * @author young
 */
public interface R2dbcIdSegment extends IdSegment {
    /**
     * 获取最大ID
     *
     * @return 最大ID
     */
    Mono<Long> getMaxId();

    /**
     * 获取是否溢出
     *
     * @return 是否溢出
     */
    default Mono<Boolean> isOverflow() {
        return getMaxId()
                .map(maxId -> getSequence() >= maxId);
    }

    /**
     * 获取是否溢出
     *
     * @param nextSeq next seq
     * @return 是否溢出
     */
    default Mono<Boolean> isOverflow(final long nextSeq) {
        return getMaxId()
                .map(maxId -> SEQUENCE_OVERFLOW == nextSeq || nextSeq > maxId);
    }

    /**
     * 获取是否可用
     *
     * @return 是否可用
     */
    default Mono<Boolean> isAvailable() {
        if (isExpired()) {
            return Mono.just(false);
        }
        return isOverflow()
                .map(ret -> !ret);
    }

    Mono<Long> incrementAndGet();

    default Mono<Void> ensureNextIdSegment(@Nonnull final IdSegment next) {
        if (compareTo(next) >= 0) {
            return Mono.error(new NextIdSegmentExpiredException(this, next));
        }
        return Mono.empty();
    }
}
