package com.querydsl.r2dbc.dml;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.ParamNotSetException;
import com.querydsl.core.types.Path;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.core.dml.R2dbcDmlClause;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.types.Null;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Statement;
import org.springframework.data.relational.core.mapping.Table;
import reactor.core.publisher.Mono;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractR2dbcClause<C extends AbstractR2dbcClause<C>> implements R2dbcDmlClause<C> {
    private static final Map<Class<?>, RelationalPath<?>> relationalPathCache = Maps.newConcurrentMap();
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
            if (o instanceof EnumValue ev) {
                o = ev.getVal();
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

    protected RelationalPath<?> buildRelationalPath(@Nonnull final EntityPath<?> entityPath) {
        return relationalPathCache.computeIfAbsent(entityPath.getType(), type -> {
            String schema = null, table = null;
            final Table tableAnn = type.getAnnotation(Table.class);
            if (Objects.nonNull(tableAnn)) {
                //schema
                if (!Strings.isNullOrEmpty(tableAnn.schema())) {
                    schema = tableAnn.schema();
                }
                //table
                String tableVal, tableName = null;
                if (!Strings.isNullOrEmpty(tableVal = tableAnn.value()) || !Strings.isNullOrEmpty(tableName = tableAnn.name())) {
                    if (!Strings.isNullOrEmpty(tableVal)) {
                        table = tableVal;
                    } else if (!Strings.isNullOrEmpty(tableName)) {
                        table = tableName;
                    }
                }
            }
            if (Strings.isNullOrEmpty(table)) {
                final Package p = type.getPackage();
                if (Objects.nonNull(p) && !Strings.isNullOrEmpty(p.getName())) {
                    final String pn = p.getName();
                    table = type.getName().substring(pn.length() + 1);
                } else {
                    table = type.getName();
                }
            }
            return new RelationalPathBase<>(entityPath.getType(), entityPath.getMetadata(), schema, table);
        });
    }
}
