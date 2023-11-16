package com.querydsl.r2dbc.dml;

import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;

import javax.annotation.Nonnull;

public class R2dbcInsertClause extends AbstractR2dbcInsertClause<R2dbcInsertClause> {

    public R2dbcInsertClause(@Nonnull final R2dbcConnectionProvider provider,
                             @Nonnull final Configuration configuration,
                             @Nonnull final RelationalPath<?> entity) {
        super(provider, configuration, entity);
    }
}
