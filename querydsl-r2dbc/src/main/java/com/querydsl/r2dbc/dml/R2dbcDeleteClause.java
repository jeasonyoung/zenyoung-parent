package com.querydsl.r2dbc.dml;

import com.querydsl.core.types.EntityPath;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.sql.Configuration;

import javax.annotation.Nonnull;

public class R2dbcDeleteClause extends AbstractR2dbcDeleteClause<R2dbcDeleteClause> {
    public R2dbcDeleteClause(@Nonnull final R2dbcConnectionProvider provider,
                             @Nonnull final Configuration configuration,
                             @Nonnull final EntityPath<?> entity) {
        super(provider, configuration, entity);
    }
}