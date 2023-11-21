package com.querydsl.r2dbc;

import com.querydsl.core.types.*;
import com.querydsl.r2dbc.core.R2dbcFetchable;

import javax.annotation.Nonnull;

public interface Union<T> extends SubQueryExpression<T>, R2dbcFetchable<T> {
    Union<T> groupBy(@Nonnull final Expression<?>... o);

    Union<T> having(@Nonnull final Predicate... o);

    Union<T> orderBy(@Nonnull final OrderSpecifier<?>... o);

    Expression<T> as(@Nonnull final String alias);

    Expression<T> as(@Nonnull final Path<T> alias);
}
