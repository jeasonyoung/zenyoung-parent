package com.querydsl.r2dbc.core.dml;

import com.querydsl.core.FilteredClause;

/**
 * {@code R2dbcDeleteClause} R2dbc Delete子句通用接口
 *
 * @param <C> 子类型
 */
public interface R2dbcDeleteClause<C extends R2dbcDeleteClause<C>>
        extends R2dbcDmlClause<C>, FilteredClause<C> {

}
