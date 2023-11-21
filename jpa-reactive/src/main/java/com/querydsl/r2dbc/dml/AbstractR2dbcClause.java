package com.querydsl.r2dbc.dml;

import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.ParamNotSetException;
import com.querydsl.core.types.Path;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.core.dml.R2dbcDmlClause;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.types.Null;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractR2dbcClause<C extends AbstractR2dbcClause<C>> implements R2dbcDmlClause<C> {
    protected final Configuration configuration;
    private final R2dbcConnectionProvider provider;
    protected boolean useLiterals;

    protected AbstractR2dbcClause(final R2dbcConnectionProvider provider, final Configuration configuration) {
        this.provider = provider;
        this.configuration = configuration;
        this.useLiterals = configuration.getUseLiterals();
    }

    @SuppressWarnings({"unchecked"})
    protected final C self() {
        return (C) this;
    }

    public final void setUseLiterals(final boolean useLiterals) {
        this.useLiterals = useLiterals;
    }

    public final C withUseLiterals() {
        setUseLiterals(true);
        return self();
    }

    protected final Mono<Connection> requireConnection() {
        if (Objects.nonNull(this.provider)) {
            return provider.getConnection();
        }
        return Mono.error(new IllegalStateException("No connection provided"));
    }

    protected final void setParameters(@Nonnull final Statement stmt,
                                       @Nonnull final List<?> objects,
                                       @Nonnull final List<Path<?>> constantPaths,
                                       @Nonnull final Map<ParamExpression<?>, ?> params,
                                       final int offset) {
        if (objects.size() != constantPaths.size()) {
            throw new IllegalArgumentException("Expected " + objects.size() + " paths, " + "but got " + constantPaths.size());
        }
        for (int i = 0; i < objects.size(); i++) {
            Object o = objects.get(i);
            if (o instanceof ParamExpression) {
                if (!params.containsKey(o)) {
                    throw new ParamNotSetException((ParamExpression<?>) o);
                }
                o = params.get(o);
            }
            bind(stmt, constantPaths.get(i), (offset * objects.size()) + i, o);
        }
    }

    private <T> void bind(@Nonnull final Statement stmt, @Nullable final Path<?> path, final int i, @Nullable final T val) {
        if (val == null || val instanceof Null) {
            if (path != null) {
                stmt.bindNull(i, path.getType());
            }
        } else {
            stmt.bind(i, val);
        }
    }
}
