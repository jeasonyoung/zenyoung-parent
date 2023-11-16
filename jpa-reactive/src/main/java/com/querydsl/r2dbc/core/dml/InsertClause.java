package com.querydsl.r2dbc.core.dml;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;

import javax.annotation.Nonnull;

public interface InsertClause<C extends InsertClause<C>> extends StoreClause<C> {
    C columns(@Nonnull final Path<?>... columns);

    C select(@Nonnull final SubQueryExpression<?> subQuery);

    C values(@Nonnull final Object... vals);
}
