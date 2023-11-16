package com.querydsl.r2dbc.core.dml;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface StoreClause<C extends StoreClause<C>> extends DMLClause<C> {
    <T> C set(@Nonnull final Path<T> path, @Nullable final T value);

    <T> C set(@Nonnull final Path<T> path, @Nonnull final Expression<? extends T> expression);

    <T> C setNull(@Nonnull final Path<T> path);

    boolean isEmpty();
}
