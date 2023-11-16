package com.querydsl.r2dbc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.querydsl.core.JoinFlag;
import com.querydsl.core.Query;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.support.QueryMixin;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.r2dbc.core.FetchableQuery;
import com.querydsl.r2dbc.core.support.FetchableSubQueryBase;
import com.querydsl.sql.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ProjectableR2dbcQuery<T, Q extends ProjectableR2dbcQuery<T, Q> & Query<Q>>
        extends FetchableSubQueryBase<T, Q> implements SQLCommonQuery<Q>, FetchableQuery<T, Q> {
    private static final Path<?> defaultQueryAlias = ExpressionUtils.path(Object.class, "query");
    protected final Configuration configuration;
    @Nullable
    protected Expression<?> union;
    protected SubQueryExpression<?> firstUnionSubQuery;
    protected boolean unionAll;

    @SuppressWarnings({"unchecked"})
    public ProjectableR2dbcQuery(@Nonnull final QueryMixin<Q> queryMixin, @Nonnull final Configuration configuration) {
        super(queryMixin);
        this.queryMixin.setSelf((Q) this);
        this.configuration = configuration;
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public <R, C> R accept(@Nonnull final Visitor<R, C> v, @Nullable final C context) {
        if (union != null) {
            return union.accept(v, context);
        } else {
            return super.accept(v, context);
        }
    }

    @Override
    public Q addJoinFlag(@Nonnull final String flag) {
        return addJoinFlag(flag, JoinFlag.Position.BEFORE_TARGET);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Q addJoinFlag(@Nonnull final String flag, @Nonnull final JoinFlag.Position position) {
        queryMixin.addJoinFlag(new JoinFlag(flag, position));
        return (Q) this;
    }

    @Override
    public Q addFlag(@Nonnull final QueryFlag.Position position, @Nonnull final String prefix, @Nonnull final Expression<?> expr) {
        final Expression<?> flag = Expressions.template(expr.getType(), prefix + "{0}", expr);
        return queryMixin.addFlag(new QueryFlag(position, flag));
    }

    public Q addFlag(@Nonnull final QueryFlag flag) {
        return queryMixin.addFlag(flag);
    }

    @Override
    public Q addFlag(@Nonnull final QueryFlag.Position position, @Nonnull final String flag) {
        return queryMixin.addFlag(new QueryFlag(position, flag));
    }

    @Override
    public Q addFlag(@Nonnull final QueryFlag.Position position, @Nonnull final Expression<?> flag) {
        return queryMixin.addFlag(new QueryFlag(position, flag));
    }

    public Q from(@Nonnull final Expression<?> arg) {
        return queryMixin.from(arg);
    }

    @Override
    public Q from(@Nonnull final Expression<?>... args) {
        return queryMixin.from(args);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Q from(@Nonnull final SubQueryExpression<?> subQuery, @Nonnull final Path<?> alias) {
        return (Q) queryMixin.from(ExpressionUtils.as((Expression) subQuery, alias));
    }

    @Override
    public Q fullJoin(@Nonnull final EntityPath<?> target) {
        return queryMixin.fullJoin(target);
    }

    @Override
    public <E> Q fullJoin(@Nonnull final EntityPath<E> target, @Nonnull final Path<E> alias) {
        return queryMixin.fullJoin(target, alias);
    }

    @Override
    public <E> Q fullJoin(@Nonnull final RelationalFunctionCall<E> target, @Nonnull final Path<E> alias) {
        return queryMixin.fullJoin(target, alias);
    }

    @Override
    public Q fullJoin(@Nonnull final SubQueryExpression<?> target, @Nonnull final Path<?> alias) {
        return queryMixin.fullJoin(target, alias);
    }

    @Override
    public <E> Q fullJoin(@Nonnull final ForeignKey<E> key, @Nonnull final RelationalPath<E> entity) {
        return queryMixin.fullJoin(entity).on(key.on(entity));
    }

    @Override
    public Q innerJoin(@Nonnull final EntityPath<?> target) {
        return queryMixin.innerJoin(target);
    }

    @Override
    public <E> Q innerJoin(@Nonnull final EntityPath<E> target, @Nonnull final Path<E> alias) {
        return queryMixin.innerJoin(target, alias);
    }

    @Override
    public <E> Q innerJoin(@Nonnull final RelationalFunctionCall<E> target, @Nonnull final Path<E> alias) {
        return queryMixin.innerJoin(target, alias);
    }

    @Override
    public Q innerJoin(@Nonnull final SubQueryExpression<?> target, @Nonnull final Path<?> alias) {
        return queryMixin.innerJoin(target, alias);
    }

    @Override
    public <E> Q innerJoin(@Nonnull final ForeignKey<E> key, @Nonnull final RelationalPath<E> entity) {
        return queryMixin.innerJoin(entity).on(key.on(entity));
    }

    @Override
    public Q join(@Nonnull final EntityPath<?> target) {
        return queryMixin.join(target);
    }

    @Override
    public <E> Q join(@Nonnull final EntityPath<E> target, @Nonnull final Path<E> alias) {
        return queryMixin.join(target, alias);
    }

    @Override
    public <E> Q join(@Nonnull final RelationalFunctionCall<E> target, @Nonnull final Path<E> alias) {
        return queryMixin.join(target, alias);
    }

    @Override
    public Q join(@Nonnull final SubQueryExpression<?> target, @Nonnull final Path<?> alias) {
        return queryMixin.join(target, alias);
    }

    @Override
    public <E> Q join(@Nonnull final ForeignKey<E> key, @Nonnull final RelationalPath<E> entity) {
        return queryMixin.join(entity).on(key.on(entity));
    }

    @Override
    public Q leftJoin(@Nonnull final EntityPath<?> target) {
        return queryMixin.leftJoin(target);
    }

    @Override
    public <E> Q leftJoin(@Nonnull final EntityPath<E> target, @Nonnull final Path<E> alias) {
        return queryMixin.leftJoin(target, alias);
    }

    @Override
    public <E> Q leftJoin(@Nonnull final RelationalFunctionCall<E> target, @Nonnull final Path<E> alias) {
        return queryMixin.leftJoin(target, alias);
    }

    @Override
    public Q leftJoin(@Nonnull final SubQueryExpression<?> target, @Nonnull final Path<?> alias) {
        return queryMixin.leftJoin(target, alias);
    }

    @Override
    public <E> Q leftJoin(@Nonnull final ForeignKey<E> key, @Nonnull final RelationalPath<E> entity) {
        return queryMixin.leftJoin(entity).on(key.on(entity));
    }

    @Override
    public Q rightJoin(@Nonnull final EntityPath<?> target) {
        return queryMixin.rightJoin(target);
    }

    @Override
    public <E> Q rightJoin(@Nonnull final EntityPath<E> target, @Nonnull final Path<E> alias) {
        return queryMixin.rightJoin(target, alias);
    }

    @Override
    public <E> Q rightJoin(@Nonnull final RelationalFunctionCall<E> target, @Nonnull final Path<E> alias) {
        return queryMixin.rightJoin(target, alias);
    }

    @Override
    public Q rightJoin(@Nonnull final SubQueryExpression<?> target, @Nonnull final Path<?> alias) {
        return queryMixin.rightJoin(target, alias);
    }

    @Override
    public <E> Q rightJoin(@Nonnull final ForeignKey<E> key, @Nonnull final RelationalPath<E> entity) {
        return queryMixin.rightJoin(entity).on(key.on(entity));
    }

    public Q on(@Nonnull final Predicate condition) {
        return queryMixin.on(condition);
    }

    @Override
    public Q on(@Nonnull final Predicate... conditions) {
        return queryMixin.on(conditions);
    }

    public <R> Union<R> union(@Nonnull final SubQueryExpression<R>... sq) {
        return union(Lists.newArrayList(sq));
    }

    public <R> Union<R> union(@Nonnull final List<SubQueryExpression<R>> sq) {
        return innerUnion(sq);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <R> Q union(@Nonnull final Path<?> alias, @Nonnull final SubQueryExpression<R>... sq) {
        return (Q) from(UnionUtils.union(ImmutableList.copyOf(sq), (Path) alias, false));
    }

    public <R> Union<R> unionAll(@Nonnull final SubQueryExpression<R>... sq) {
        return unionAll(Lists.newArrayList(sq));
    }

    public <R> Union<R> unionAll(@Nonnull final List<SubQueryExpression<R>> sq) {
        unionAll = true;
        return innerUnion(sq);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <R> Q unionAll(@Nonnull final Path<?> alias, @Nonnull final SubQueryExpression<R>... sq) {
        return (Q) from(UnionUtils.union(ImmutableList.copyOf(sq), (Path) alias, true));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <R> Union<R> innerUnion(@Nonnull final List<SubQueryExpression<R>> sq) {
        queryMixin.setProjection(sq.get(0).getMetadata().getProjection());
        if (!queryMixin.getMetadata().getJoins().isEmpty()) {
            throw new IllegalArgumentException("Don't mix union and from");
        }
        this.union = UnionUtils.union(sq, unionAll);
        this.firstUnionSubQuery = sq.get(0);
        return new UnionImpl(this);
    }

    @Override
    public Q withRecursive(@Nonnull final Path<?> alias, @Nonnull final SubQueryExpression<?> query) {
        queryMixin.addFlag(new QueryFlag(QueryFlag.Position.WITH, SQLTemplates.RECURSIVE));
        return with(alias, query);
    }

    @Override
    public Q withRecursive(@Nonnull final Path<?> alias, @Nonnull final Expression<?> query) {
        queryMixin.addFlag(new QueryFlag(QueryFlag.Position.WITH, SQLTemplates.RECURSIVE));
        return with(alias, query);
    }

    @Override
    public WithBuilder<Q> withRecursive(@Nonnull final Path<?> alias, @Nonnull final Path<?>... columns) {
        queryMixin.addFlag(new QueryFlag(QueryFlag.Position.WITH, SQLTemplates.RECURSIVE));
        return with(alias, columns);
    }

    @Override
    public Q with(@Nonnull final Path<?> alias, @Nonnull final SubQueryExpression<?> query) {
        final Expression<?> expr = ExpressionUtils.operation(alias.getType(), SQLOps.WITH_ALIAS, alias, query);
        return queryMixin.addFlag(new QueryFlag(QueryFlag.Position.WITH, expr));
    }

    @Override
    public Q with(@Nonnull final Path<?> alias, @Nonnull final Expression<?> query) {
        final Expression<?> expr = ExpressionUtils.operation(alias.getType(), SQLOps.WITH_ALIAS, alias, query);
        return queryMixin.addFlag(new QueryFlag(QueryFlag.Position.WITH, expr));
    }

    @Override
    public WithBuilder<Q> with(@Nonnull final Path<?> alias, @Nonnull final Path<?>... columns) {
        final Expression<?> columnsCombined = ExpressionUtils.list(Object.class, columns);
        final Expression<?> aliasCombined = Expressions.operation(alias.getType(), SQLOps.WITH_COLUMNS, alias, columnsCombined);
        return new WithBuilder<>(queryMixin, aliasCombined);
    }

    protected void clone(@Nonnull final Q query) {
        this.union = query.union;
        this.unionAll = query.unionAll;
        this.firstUnionSubQuery = query.firstUnionSubQuery;
    }

    @Override
    public abstract Q clone();

    protected abstract SQLSerializer createSerializer();

    private Set<Path<?>> getRootPaths(@Nonnull final Collection<? extends Expression<?>> exprs) {
        final Set<Path<?>> paths = Sets.newHashSet();
        for (Expression<?> e : exprs) {
            final Path<?> path = e.accept(PathExtractor.DEFAULT, null);
            if (path != null && !path.getMetadata().isRoot()) {
                paths.add(path.getMetadata().getRootPath());
            }
        }
        return paths;
    }

    private Collection<? extends Expression<?>> expandProjection(@Nonnull final Expression<?> expr) {
        if (expr instanceof FactoryExpression) {
            return ((FactoryExpression<?>) expr).getArgs();
        } else {
            return ImmutableList.of(expr);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected SQLSerializer serialize(final boolean forCountRow) {
        final SQLSerializer serializer = createSerializer();
        if (union != null) {
            if (queryMixin.getMetadata().getProjection() == null ||
                    expandProjection(queryMixin.getMetadata().getProjection()).equals(expandProjection(firstUnionSubQuery.getMetadata().getProjection()))) {
                serializer.serializeUnion(union, queryMixin.getMetadata(), unionAll);
            } else {
                final QueryMixin<Q> mixin2 = new QueryMixin<>(queryMixin.getMetadata().clone());
                final Set<Path<?>> paths = getRootPaths(expandProjection(mixin2.getMetadata().getProjection()));
                if (paths.isEmpty()) {
                    mixin2.from(ExpressionUtils.as((Expression) union, defaultQueryAlias));
                } else if (paths.size() == 1) {
                    mixin2.from(ExpressionUtils.as((Expression) union, paths.iterator().next()));
                } else {
                    throw new IllegalStateException("Unable to create serialize union");
                }
                serializer.serialize(mixin2.getMetadata(), forCountRow);
            }
        } else {
            serializer.serialize(queryMixin.getMetadata(), forCountRow);
        }
        return serializer;
    }

    public SQLBindings getSQL() {
        return getSQL(serialize(false));
    }

    protected SQLBindings getSQL(@Nonnull final SQLSerializer serializer) {
        final List<Object> args = Lists.newArrayList();
        final Map<ParamExpression<?>, Object> params = getMetadata().getParams();
        for (Object o : serializer.getConstants()) {
            if (o instanceof ParamExpression) {
                if (!params.containsKey(o)) {
                    throw new ParamNotSetException((ParamExpression<?>) o);
                }
                o = queryMixin.getMetadata().getParams().get(o);
            }
            args.add(o);
        }
        return new SQLBindings(serializer.toString(), args);
    }

    @Override
    public String toString() {
        SQLSerializer serializer = serialize(false);
        return serializer.toString().trim();
    }
}
