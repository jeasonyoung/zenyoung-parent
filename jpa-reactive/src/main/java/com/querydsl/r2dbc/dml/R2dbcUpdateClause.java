package com.querydsl.r2dbc.dml;

import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;

import javax.annotation.Nonnull;

public class R2dbcUpdateClause extends AbstractR2dbcUpdateClause<R2dbcUpdateClause> {
    public R2dbcUpdateClause(@Nonnull final R2dbcConnectionProvider provider,
                             @Nonnull final Configuration configuration,
                             @Nonnull final RelationalPath<?> entity) {
        super(provider, configuration, entity);
    }
}
