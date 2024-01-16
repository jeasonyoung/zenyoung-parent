package top.zenyoung.segment;

import top.zenyoung.segment.exception.NextIdSegmentExpiredException;

import javax.annotation.Nonnull;

/**
 * ID分段
 *
 * @author young
 */
public interface JdbcIdSegment extends IdSegment {
    /**
     * 获取最大ID
     *
     * @return 最大ID
     */
    long getMaxId();

    /**
     * 获取是否溢出
     *
     * @return 是否溢出
     */
    default boolean isOverflow() {
        return getSequence() >= getMaxId();
    }

    /**
     * 获取是否溢出
     *
     * @param nextSeq next seq
     * @return 是否溢出
     */
    default boolean isOverflow(final long nextSeq) {
        return nextSeq == SEQUENCE_OVERFLOW || nextSeq > getMaxId();
    }

    long incrementAndGet();

    /**
     * 获取是否可用
     *
     * @return 是否可用
     */
    default boolean isAvailable() {
        final boolean isExpired = isExpired(), isOverflow = isOverflow();
        return !isExpired && !isOverflow;
    }

    default void ensureNextIdSegment(@Nonnull final IdSegment next) {
        if (compareTo(next) >= 0) {
            throw new NextIdSegmentExpiredException(this, next);
        }
    }
}
