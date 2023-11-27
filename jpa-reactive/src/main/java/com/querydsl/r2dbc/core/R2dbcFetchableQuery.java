package com.querydsl.r2dbc.core;

import com.querydsl.core.ResultTransformer;
import com.querydsl.core.SimpleQuery;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;

import javax.annotation.Nonnull;

/**
 * R2dbc 查询数据
 *
 * @param <T> entity type
 * @param <Q> Query type
 * @author young
 */
public interface R2dbcFetchableQuery<T, Q extends R2dbcFetchableQuery<T, Q>> extends SimpleQuery<Q>, R2dbcFetchable<T> {
    /**
     * Change the projection of this query
     *
     * @param expr new projection
     * @param <U>  entity type
     * @return the current object
     */
    <U> R2dbcFetchableQuery<U, ?> select(@Nonnull final Expression<U> expr);

    /**
     * Change the projection of this query
     *
     * @param exprs new projection
     * @return the current object
     */
    R2dbcFetchableQuery<Tuple, ?> select(@Nonnull final Expression<?>... exprs);

    /**
     * Apply the given transformer to this FetchableQuery instance and return the results
     *
     * @param transformer result transformer
     * @param <S>         entity type
     * @return transformed result
     */
    <S> S transform(@Nonnull final ResultTransformer<S> transformer);
}
