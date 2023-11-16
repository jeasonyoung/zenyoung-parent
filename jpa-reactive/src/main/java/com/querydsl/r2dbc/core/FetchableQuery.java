package com.querydsl.r2dbc.core;

import com.querydsl.core.ResultTransformer;
import com.querydsl.core.SimpleQuery;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;

import javax.annotation.Nonnull;

public interface FetchableQuery<T, Q extends FetchableQuery<T, Q>> extends SimpleQuery<Q>, Fetchable<T> {
    <U> FetchableQuery<U, ?> select(@Nonnull final Expression<U> expr);

    FetchableQuery<Tuple, ?> select(@Nonnull final Expression<?>... exprs);

    <S> S transform(@Nonnull final ResultTransformer<S> transformer);
}
