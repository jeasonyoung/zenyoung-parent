package com.querydsl.r2dbc.core.support;

import com.querydsl.core.FetchableQuery;
import com.querydsl.core.ResultTransformer;
import com.querydsl.core.support.QueryBase;
import com.querydsl.core.support.QueryMixin;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.r2dbc.core.R2dbcFetchable;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public abstract class R2dbcFetchableQueryBase<T, Q extends R2dbcFetchableQueryBase<T, Q>>
        extends QueryBase<Q> implements R2dbcFetchable<T> {
    protected R2dbcFetchableQueryBase(@Nonnull final QueryMixin<Q> queryMixin) {
        super(queryMixin);
    }

    @Override
    public Mono<T> fetchFirst() {
        return super.limit(1).fetchOne();
    }

    @Override
    public Mono<T> fetchOne() {
        return fetchFirst();
    }

    public <M> M transform(@Nonnull final ResultTransformer<M> transformer) {
        return transformer.transform((FetchableQuery<?, ?>) this);
    }

    @Override
    public final boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof SubQueryExpression<?> expr) {
            return expr.getMetadata().equals(queryMixin.getMetadata());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
