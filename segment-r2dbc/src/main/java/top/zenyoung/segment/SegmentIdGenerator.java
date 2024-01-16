package top.zenyoung.segment;

import reactor.core.publisher.Mono;

/**
 * 分段编码生成器
 *
 * @author young
 */
public interface SegmentIdGenerator {
    /**
     * 生成序号数据
     *
     * @return 序号数据
     */
    Mono<Long> nextId();
}
