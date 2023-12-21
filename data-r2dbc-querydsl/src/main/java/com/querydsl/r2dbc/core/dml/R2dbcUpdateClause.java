package com.querydsl.r2dbc.core.dml;

import com.querydsl.core.FilteredClause;
import com.querydsl.core.types.Path;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * {@code R2dbcInsertClause} R2dbc Update 子句定义通用接口
 *
 * @param <C> 子类型
 * @author young
 */
public interface R2dbcUpdateClause<C extends R2dbcUpdateClause<C>> extends R2dbcStoreClause<C>, FilteredClause<C> {

    /**
     * Set the paths to be updated
     *
     * @param paths paths to be updated
     * @param vals  values to be set
     * @return the current object
     */
    C set(@Nonnull final List<? extends Path<?>> paths, @Nonnull final List<?> vals);
}
