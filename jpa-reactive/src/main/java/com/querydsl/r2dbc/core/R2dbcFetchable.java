package com.querydsl.r2dbc.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * R2dbc 查询数据
 *
 * @param <T> 数据模型
 */
public interface R2dbcFetchable<T> {
    /**
     * 查询数据集合
     *
     * @return 查询结果
     */
    Flux<T> fetch();

    /**
     * 查询第一条记录
     *
     * @return 第一条记录
     */
    Mono<T> fetchFirst();

    /**
     * 查询一条记录
     *
     * @return 一条记录
     */
    Mono<T> fetchOne();
}
