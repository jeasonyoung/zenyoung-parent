package com.querydsl.r2dbc.mysql;

import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;

import javax.annotation.Nonnull;

public class MySqlR2dbcQuery<T> extends AbstractMySqlR2dbcQuery<T, MySqlR2dbcQuery<T>> {

    public MySqlR2dbcQuery(@Nonnull final R2dbcConnectionProvider provider) {
        this(provider, new Configuration(MySQLTemplates.DEFAULT), new DefaultQueryMetadata());
    }

    public MySqlR2dbcQuery(@Nonnull final R2dbcConnectionProvider provider, @Nonnull final SQLTemplates templates) {
        this(provider, new Configuration(templates), new DefaultQueryMetadata());
    }

    public MySqlR2dbcQuery(@Nonnull final R2dbcConnectionProvider provider, @Nonnull final Configuration configuration, @Nonnull final QueryMetadata metadata) {
        super(provider, configuration, metadata);
    }

    public MySqlR2dbcQuery(@Nonnull final R2dbcConnectionProvider provider, @Nonnull final Configuration configuration) {
        super(provider, configuration, new DefaultQueryMetadata());
    }

    @Override
    public MySqlR2dbcQuery<T> clone(@Nonnull final R2dbcConnectionProvider provider) {
        final MySqlR2dbcQuery<T> q = new MySqlR2dbcQuery<>(provider, getConfiguration(), getMetadata().clone());
        q.clone(this);
        return q;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <U> MySqlR2dbcQuery<U> select(@Nonnull final Expression<U> expr) {
        queryMixin.setProjection(expr);
        // This is the new type
        return (MySqlR2dbcQuery<U>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MySqlR2dbcQuery<Tuple> select(@Nonnull final Expression<?>... exprs) {
        queryMixin.setProjection(exprs);
        // This is the new type
        return (MySqlR2dbcQuery<Tuple>) this;
    }
}
