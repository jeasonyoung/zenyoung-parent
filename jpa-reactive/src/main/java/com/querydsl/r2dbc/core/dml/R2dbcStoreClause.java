package com.querydsl.r2dbc.core.dml;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * R2dbc Insert 和 Update 子句通用接口
 *
 * @param <C> 子类型
 * @author young
 */
public interface R2dbcStoreClause<C extends R2dbcStoreClause<C>> extends R2dbcDmlClause<C> {
    /**
     * Add a value binding
     *
     * @param path  path to be updated
     * @param value value to set
     * @param <T>   数据类型
     * @return the current object
     */
    <T> C set(@Nonnull final Path<T> path, @Nullable final T value);

    /**
     * Add an expression binding
     *
     * @param <T>        数据类型
     * @param path       path to be updated
     * @param expression binding
     * @return the current object
     */
    <T> C set(@Nonnull final Path<T> path, @Nonnull final Expression<? extends T> expression);

    /**
     * Bind the given path to null
     *
     * @param path path to be updated
     * @return the current object
     */
    <T> C setNull(@Nonnull final Path<T> path);

    /**
     * Returns true, if no bindings have been set, otherwise false.
     *
     * @return true, if empty, false, if not
     */
    boolean isEmpty();
}
