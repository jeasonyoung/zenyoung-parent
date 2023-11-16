package com.querydsl.r2dbc.core.support;

import com.querydsl.core.QueryMetadata;
import com.querydsl.core.support.ExtendedSubQuery;
import com.querydsl.core.support.QueryMixin;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanOperation;
import com.querydsl.core.types.dsl.Expressions;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

public abstract class FetchableSubQueryBase<T, Q extends FetchableSubQueryBase<T, Q>>
        extends FetchableQueryBase<T, Q> implements ExtendedSubQuery<T> {
    private final SubQueryExpression<T> mixin;

    @SuppressWarnings({"unchecked"})
    public FetchableSubQueryBase(@Nonnull final QueryMixin<Q> queryMixin) {
        super(queryMixin);
        this.mixin = new SubQueryExpressionImpl<>((Class<T>) Object.class, queryMixin.getMetadata());
    }

    @Override
    public BooleanExpression contains(@Nonnull final Expression<? extends T> right) {
        return Expressions.predicate(Ops.IN, right, this);
    }

    @Override
    public BooleanExpression contains(@Nonnull final T constant) {
        return contains(Expressions.constant(constant));
    }

    @Override
    public BooleanExpression exists() {
        final QueryMetadata metadata = getMetadata();
        if (metadata.getProjection() == null) {
            queryMixin.setProjection(Expressions.ONE);
        }
        return Expressions.predicate(Ops.EXISTS, this);
    }

    @Override
    public BooleanExpression eq(@Nonnull final Expression<? extends T> expr) {
        return Expressions.predicate(Ops.EQ, this, expr);
    }

    @Override
    public BooleanExpression eq(@Nonnull final T constant) {
        return eq(Expressions.constant(constant));
    }

    @Override
    public BooleanExpression ne(@Nonnull final Expression<? extends T> expr) {
        return Expressions.predicate(Ops.NE, this, expr);
    }

    @Override
    public BooleanExpression ne(@Nonnull final T constant) {
        return ne(Expressions.constant(constant));
    }

    @Override
    public BooleanExpression notExists() {
        return exists().not();
    }

    @Override
    public BooleanExpression lt(@Nonnull final Expression<? extends T> expr) {
        return Expressions.predicate(Ops.LT, this, expr);
    }

    @Override
    public BooleanExpression lt(@Nonnull final T constant) {
        return lt(Expressions.constant(constant));
    }

    @Override
    public BooleanExpression gt(@Nonnull final Expression<? extends T> expr) {
        return Expressions.predicate(Ops.GT, this, expr);
    }

    @Override
    public BooleanExpression gt(@Nonnull final T constant) {
        return gt(Expressions.constant(constant));
    }

    @Override
    public BooleanExpression loe(@Nonnull final Expression<? extends T> expr) {
        return Expressions.predicate(Ops.LOE, this, expr);
    }

    @Override
    public BooleanExpression loe(@Nonnull final T constant) {
        return loe(Expressions.constant(constant));
    }

    @Override
    public BooleanExpression goe(@Nonnull final Expression<? extends T> expr) {
        return Expressions.predicate(Ops.GOE, this, expr);
    }

    @Override
    public BooleanExpression goe(@Nonnull final T constant) {
        return goe(Expressions.constant(constant));
    }

    @Override
    public BooleanOperation isNull() {
        return Expressions.booleanOperation(Ops.IS_NULL, mixin);
    }

    @Override
    public BooleanOperation isNotNull() {
        return Expressions.booleanOperation(Ops.IS_NOT_NULL, mixin);
    }

    @Override
    public final int hashCode() {
        return mixin.hashCode();
    }

    @Override
    public final QueryMetadata getMetadata() {
        return queryMixin.getMetadata();
    }

    @Override
    public <R, C> R accept(@Nonnull final Visitor<R, C> v, C context) {
        return mixin.accept(v, context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        final Expression<?> projection = queryMixin.getMetadata().getProjection();
        return (Class<T>) (projection != null ? projection.getType() : Void.class);
    }

    @Override
    public BooleanExpression in(@Nonnull final Collection<? extends T> right) {
        if (right.size() == 1) {
            return eq(right.iterator().next());
        } else {
            return Expressions.booleanOperation(Ops.IN, mixin, ConstantImpl.create(right));
        }
    }

    @Override
    public BooleanExpression in(@Nonnull final T... right) {
        return this.in(Arrays.asList(right));
    }
}
