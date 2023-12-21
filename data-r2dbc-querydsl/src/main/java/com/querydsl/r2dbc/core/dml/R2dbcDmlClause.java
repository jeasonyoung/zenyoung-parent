package com.querydsl.r2dbc.core.dml;

import reactor.core.publisher.Mono;

/**
 * R2dbc DML 父接口
 *
 * @param <C> 子类型
 * @author young
 */
public interface R2dbcDmlClause<C extends R2dbcDmlClause<C>> {
    /**
     * 执行子句并返回受影响的行数
     *
     * @return 受影响的行数
     */
    Mono<Long> execute();
}
