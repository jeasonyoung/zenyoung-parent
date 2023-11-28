package com.querydsl.r2dbc;

import com.querydsl.core.QueryFactory;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.r2dbc.dml.R2dbcDeleteClause;
import com.querydsl.r2dbc.dml.R2dbcInsertClause;
import com.querydsl.r2dbc.dml.R2dbcUpdateClause;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLCommonQuery;

import javax.annotation.Nonnull;

public abstract class AbstractR2dbcQueryFactory<Q extends SQLCommonQuery<?>> implements QueryFactory<Q> {
    protected final Configuration configuration;
    protected final R2dbcConnectionProvider provider;

    protected AbstractR2dbcQueryFactory(@Nonnull final R2dbcConnectionProvider provider, @Nonnull final Configuration configuration) {
        this.configuration = configuration;
        this.provider = provider;
    }

    public final Configuration getConfiguration() {
        return configuration;
    }

    public final R2dbcConnectionProvider getProvider() {
        return provider;
    }

    public final R2dbcInsertClause insert(@Nonnull final EntityPath<?> path) {
        return new R2dbcInsertClause(provider, configuration, path);
    }

    public final R2dbcUpdateClause update(@Nonnull final EntityPath<?> path) {
        return new R2dbcUpdateClause(provider, configuration, path);
    }

    public final R2dbcDeleteClause delete(final EntityPath<?> path) {
        return new R2dbcDeleteClause(provider, configuration, path);
    }

    @SuppressWarnings({"unchecked"})
    public final Q from(@Nonnull final Expression<?> from) {
        return (Q) query().from(from);
    }

    @SuppressWarnings({"unchecked"})
    public final Q from(@Nonnull final Expression<?>... args) {
        return (Q) query().from(args);
    }

    @SuppressWarnings({"unchecked"})
    public final Q from(@Nonnull final SubQueryExpression<?> subQuery, @Nonnull final Path<?> alias) {
        return (Q) query().from(subQuery, alias);
    }

    public abstract <T> AbstractR2dbcQuery<T, ?> select(@Nonnull final Expression<T> expr);

    public abstract AbstractR2dbcQuery<Tuple, ?> select(@Nonnull final Expression<?>... exprs);

    public abstract <T> AbstractR2dbcQuery<T, ?> selectDistinct(@Nonnull final Expression<T> expr);

    public abstract AbstractR2dbcQuery<Tuple, ?> selectDistinct(@Nonnull final Expression<?>... exprs);

    public abstract AbstractR2dbcQuery<Integer, ?> selectZero();

    public abstract AbstractR2dbcQuery<Integer, ?> selectOne();

    public abstract <T> AbstractR2dbcQuery<T, ?> selectFrom(@Nonnull final EntityPath<T> expr);
}
