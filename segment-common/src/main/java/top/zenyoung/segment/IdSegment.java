package top.zenyoung.segment;

import javax.annotation.Nonnull;

/**
 * ID分段接口
 *
 * @author young
 */
public interface IdSegment extends Comparable<IdSegment> {
    long SEQUENCE_OVERFLOW = -1;
    long TIME_TO_LIVE_FOREVER = Long.MAX_VALUE;

    /**
     * ID段获取时间
     *
     * @return 获取时间
     */
    long getFetchTime();

    /**
     * 获取偏移量
     *
     * @return 偏移量
     */
    long getOffset();

    /**
     * 获取序列
     *
     * @return 序列
     */
    long getSequence();

    /**
     * 获取步长
     *
     * @return 步长
     */
    long getStep();

    default long getTtl() {
        return TIME_TO_LIVE_FOREVER;
    }

    default boolean isExpired() {
        if (TIME_TO_LIVE_FOREVER == getTtl()) {
            return false;
        }
        return Clock.CACHE.secondTime() - getFetchTime() > getTtl();
    }

    @Override
    default int compareTo(@Nonnull final IdSegment o) {
        if (getOffset() == o.getOffset()) {
            return 0;
        }
        return getOffset() > o.getOffset() ? 1 : -1;
    }
}
