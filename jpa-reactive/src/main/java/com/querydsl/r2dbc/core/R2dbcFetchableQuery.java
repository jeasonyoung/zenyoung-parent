package com.querydsl.r2dbc.core;

import com.querydsl.core.ResultTransformer;
import com.querydsl.core.SimpleQuery;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;

import javax.annotation.Nonnull;

/**
 * R2dbc 查询数据
 *
 * @param <T>
 * @param <Q>
 * @author young
 */
public interface R2dbcFetchableQuery<T, Q extends R2dbcFetchableQuery<T, Q>> extends SimpleQuery<Q>, R2dbcFetchable<T> {
    <U> R2dbcFetchableQuery<U, ?> select(@Nonnull final Expression<U> expr);

    R2dbcFetchableQuery<Tuple, ?> select(@Nonnull final Expression<?>... exprs);

    <S> S transform(@Nonnull final ResultTransformer<S> transformer);
}
