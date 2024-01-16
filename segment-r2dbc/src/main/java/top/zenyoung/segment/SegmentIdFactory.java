package top.zenyoung.segment;

import reactor.core.publisher.Mono;

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
    Mono<SegmentIdGenerator> getIdGenerator(@Nonnull final String bizType);

    /**
     * 分段ID-新增
     *
     * @param bizType 业务类型
     * @param maxId   当前最大ID
     * @param step    步长
     * @param delta   每次增量
     */
    Mono<Boolean> addSegment(@Nonnull final String bizType, final long maxId, final long step, final long delta);
}
