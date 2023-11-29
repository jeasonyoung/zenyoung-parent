package com.querydsl.r2dbc.core.types.dsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Visitor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public final class OptionalExpression<T> implements Expression<Optional<T>> {
    public static <T> OptionalExpression<T> of(@Nonnull final Expression<T> expr) {
        return new OptionalExpression<>(expr);
    }

    private final Expression<T> wrapped;

    private OptionalExpression(final Expression<T> wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped);
    }


    @Override
    public <R, C> @Nullable R accept(@Nonnull final Visitor<R, C> v, @Nullable final C context) {
        return wrapped.accept(v, context);
    }

    @Override
    public Class<? extends Optional<T>> getType() {
        throw new UnsupportedOperationException("Generic optional type is not supported.");
    }

    public Class<? extends T> getWrappedType() {
        return wrapped.getType();
    }
}
