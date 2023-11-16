package com.querydsl.r2dbc;

import com.querydsl.core.QueryMetadata;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.sql.Configuration;

import javax.annotation.Nonnull;

public class R2dbcQuery<T> extends AbstractR2dbcQuery<T, R2dbcQuery<T>> {

    public R2dbcQuery(@Nonnull final R2dbcConnectionProvider provider, @Nonnull final Configuration configuration) {
        super(provider, configuration);
    }

    public R2dbcQuery(@Nonnull final R2dbcConnectionProvider provider, @Nonnull final Configuration configuration, @Nonnull final QueryMetadata metadata) {
        super(provider, configuration, metadata);
    }


    @Override
    public R2dbcQuery<T> clone(@Nonnull final R2dbcConnectionProvider provider) {
        final R2dbcQuery<T> q = new R2dbcQuery<>(provider, getConfiguration(), getMetadata().clone());
        q.clone(this);
        return q;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <U> R2dbcQuery<U> select(@Nonnull final Expression<U> expr) {
        queryMixin.setProjection(expr);
        // This is the new type
        return (R2dbcQuery<U>) this;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public R2dbcQuery<Tuple> select(@Nonnull final Expression<?>... exprs) {
        queryMixin.setProjection(exprs);
        // This is the new type
        return (R2dbcQuery<Tuple>) this;
    }
}
