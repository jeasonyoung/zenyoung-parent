package com.querydsl.r2dbc.core.dml;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;

import javax.annotation.Nonnull;

/**
 * {@code R2dbcInsertClause} R2dbc Insert 子句定义通用接口
 *
 * @param <C>
 */
public interface R2dbcInsertClause<C extends R2dbcInsertClause<C>> extends R2dbcStoreClause<C> {
    /**
     * Define the columns to be populated
     *
     * @param columns columns to be populated
     * @return the current object
     */
    C columns(@Nonnull final Path<?>... columns);

    /**
     * Define the populate via subquery
     *
     * @param subQuery sub query to be used for population
     * @return the current object
     */
    C select(@Nonnull final SubQueryExpression<?> subQuery);

    /**
     * Define the value bindings
     *
     * @param vals values to be inserted
     * @return the current object
     */
    C values(@Nonnull final Object... vals);
}
