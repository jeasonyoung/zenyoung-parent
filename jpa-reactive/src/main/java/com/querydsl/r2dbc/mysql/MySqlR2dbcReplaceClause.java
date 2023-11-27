package com.querydsl.r2dbc.mysql;

import com.querydsl.core.QueryFlag;
import com.querydsl.core.types.EntityPath;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.dml.R2dbcInsertClause;
import com.querydsl.sql.Configuration;

import javax.annotation.Nonnull;

public class MySqlR2dbcReplaceClause extends R2dbcInsertClause {
    protected static final String REPLACE_INTO = "replace into ";

    public MySqlR2dbcReplaceClause(@Nonnull final R2dbcConnectionProvider provider,
                                   @Nonnull final Configuration configuration,
                                   @Nonnull final EntityPath<?> entity) {
        super(provider, configuration, entity);
        addFlag(QueryFlag.Position.START_OVERRIDE, REPLACE_INTO);
    }
}
