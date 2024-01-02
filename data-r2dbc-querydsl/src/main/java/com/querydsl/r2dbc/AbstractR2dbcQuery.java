package com.querydsl.r2dbc;

import com.google.common.collect.Lists;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryException;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.support.QueryMixin;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.r2dbc.core.internal.R2dbcUtils;
import com.querydsl.r2dbc.core.types.dsl.OptionalExpression;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SQLTemplates;
import io.r2dbc.spi.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractR2dbcQuery<T, Q extends AbstractR2dbcQuery<T, Q>> extends ProjectableR2dbcQuery<T, Q> {
    private final R2dbcConnectionProvider provider;
    protected boolean useLiterals;

    protected AbstractR2dbcQuery(@Nonnull final R2dbcConnectionProvider provider, @Nonnull final Configuration configuration) {
        this(provider, configuration, new DefaultQueryMetadata());
    }

    protected AbstractR2dbcQuery(@Nonnull final R2dbcConnectionProvider provider, @Nonnull final Configuration configuration, @Nonnull final QueryMetadata metadata) {
        super(new QueryMixin<>(metadata, false), configuration);
        this.provider = provider;
        this.useLiterals = configuration.getUseLiterals();
    }

    public SimpleExpression<T> as(@Nonnull final String alias) {
        return Expressions.as(this, alias);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SimpleExpression<T> as(@Nonnull final Path<?> alias) {
        return Expressions.as(this, (Path) alias);
    }

    public Q forUpdate() {
        final QueryFlag forUpdateFlag = configuration.getTemplates().getForUpdateFlag();
        return addFlag(forUpdateFlag);
    }

    public Q forShare() {
        return forShare(false);
    }

    public Q forShare(final boolean fallbackToForUpdate) {
        final SQLTemplates sqlTemplates = configuration.getTemplates();
        if (sqlTemplates.isForShareSupported()) {
            final QueryFlag forShareFlag = sqlTemplates.getForShareFlag();
            return addFlag(forShareFlag);
        }
        if (fallbackToForUpdate) {
            return forUpdate();
        }
        throw new QueryException("Using forShare() is not supported");
    }

    @Override
    protected SQLSerializer createSerializer() {
        final SQLSerializer serializer = new SQLSerializer(configuration);
        serializer.setUseLiterals(useLiterals);
        return serializer;
    }

    public void setUseLiterals(final boolean useLiterals) {
        this.useLiterals = useLiterals;
    }

    @SuppressWarnings({"unchecked"})
    public Q withUseLiterals() {
        setUseLiterals(true);
        return (Q) this;
    }

    @Override
    protected void clone(@Nonnull final Q query) {
        super.clone(query);
        this.useLiterals = query.useLiterals;
    }

    @Override
    public Q clone() {
        return this.clone(this.provider);
    }

    public abstract Q clone(@Nonnull final R2dbcConnectionProvider provider);

    @Override
    public Flux<T> fetch() {
        return requireConnection()
                .flatMapMany(conn -> {
                    final Expression<T> expr = getProjection();
                    final R2dbcMapper<T> mapper = createMapper(expr);
                    final SQLSerializer serializer = serialize(false);
                    final String originalSql = serializer.toString();
                    final String sql = R2dbcUtils.replaceBindingArguments(originalSql);
                    final Statement statement = bind(conn.createStatement(sql), serializer);
                    return Flux.from(statement.execute()).flatMap(result -> result.map(mapper::map));
                });
    }

    private Mono<Connection> requireConnection() {
        if (this.provider != null) {
            return this.provider.getConnection();
        } else {
            return Mono.error(new IllegalStateException("No connection provided"));
        }
    }

    @SuppressWarnings({"unchecked"})
    private Expression<T> getProjection() {
        return (Expression<T>) queryMixin.getMetadata().getProjection();
    }

    @SuppressWarnings({"unchecked"})
    private R2dbcMapper<T> createMapper(@Nonnull final Expression<T> expr) {
        if (expr instanceof FactoryExpression) {
            final FactoryExpression<T> fe = (FactoryExpression<T>) expr;
            return (row, meta) -> newInstance(fe, row, 0);
        } else if (expr.equals(Wildcard.all)) {
            return this::toWildcardObjectArray;
        } else {
            if (expr instanceof OptionalExpression) {
                final OptionalExpression<T> oe = (OptionalExpression<T>) expr;
                return (row, meta) -> asOptional(row, oe);
            }
            return (row, meta) -> asRequired(row, expr);
        }
    }

    private T asRequired(@Nonnull final Row row, @Nonnull final Expression<T> expr) {
        return Objects.requireNonNull(row.get(0, expr.getType()), "Null result");
    }

    @SuppressWarnings({"unchecked"})
    private T asOptional(@Nonnull final Row row, @Nonnull final OptionalExpression<T> oe) {
        return (T) Optional.ofNullable(row.get(0, oe.getWrappedType()));
    }

    @Nonnull
    @SuppressWarnings({"unchecked"})
    private T toWildcardObjectArray(@Nonnull final Row row, @Nonnull final RowMetadata meta) {
        final List<? extends ColumnMetadata> metaList = Lists.newArrayList(meta.getColumnMetadatas());
        final Object[] args = new Object[metaList.size()];
        for (int i = 0; i < args.length; i++) {
            final ColumnMetadata columnMetadata = metaList.get(i);
            args[i] = row.get(i, Objects.requireNonNull(columnMetadata.getJavaType(), "Unknown Java type"));
        }
        return (T) args;
    }

    @Nonnull
    private T newInstance(@Nonnull final FactoryExpression<T> c, @Nonnull final Row rs, final int offset) {
        final Object[] args = new Object[c.getArgs().size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = get(rs, c.getArgs().get(i), offset + i);
        }
        return Objects.requireNonNull(c.newInstance(args), "Null result");
    }

    private Object get(@Nonnull final Row rs, @Nonnull final Expression<?> expr, final int i) {
        return rs.get(i, expr.getType());
    }

    private Statement bind(@Nonnull final Statement statement, @Nonnull final SQLSerializer serializer) {
        final List<Object> args = serializer.getConstants();
        for (int i = 0; i < args.size(); i++) {
            Object arg = args.get(i);
            //处理枚举
            if (arg instanceof EnumValue ev) {
                arg = ev.getVal();
            }
            //设置参数值
            statement.bind(i, arg);
        }
        return statement;
    }
}
