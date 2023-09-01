package top.zenyoung.segment;

import javax.annotation.Nonnull;

/**
 * 分段ID工厂
 *
 * @author young
 */
public interface SegmentIdFactory {

    /**
     * 根据BizType创建ID生成器
     *
     * @param bizType bizType
     * @return ID生成器
     */
    SegmentIdGenerator getIdGenerator(@Nonnull final String bizType);
}
