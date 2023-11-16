package com.querydsl.r2dbc.dml;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.querydsl.core.*;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.core.dml.UpdateClause;
import com.querydsl.r2dbc.core.internal.R2dbcUtils;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.dml.SQLUpdateBatch;
import com.querydsl.sql.types.Null;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public abstract class AbstractR2dbcUpdateClause<C extends AbstractR2dbcUpdateClause<C>>
        extends AbstractR2dbcClause<C> implements UpdateClause<C> {
    protected final RelationalPath<?> entity;
    protected final List<SQLUpdateBatch> batches = Lists.newArrayList();
    protected Map<Path<?>, Expression<?>> updates = Maps.newLinkedHashMap();
    protected QueryMetadata metadata = new DefaultQueryMetadata();

    public AbstractR2dbcUpdateClause(@Nonnull final R2dbcConnectionProvider provider,
                                     @Nonnull final Configuration configuration,
                                     @Nonnull final RelationalPath<?> entity) {
        super(provider, configuration);
        this.entity = entity;
        metadata.addJoin(JoinType.DEFAULT, entity);
    }

    @Override
    public String toString() {
        final SQLSerializer serializer = createSerializerAndSerialize();
        return serializer.toString();
    }

    public C addFlag(@Nonnull final QueryFlag.Position position, @Nonnull final String flag) {
        metadata.addFlag(new QueryFlag(position, flag));
        return self();
    }

    public C addFlag(@Nonnull final QueryFlag.Position position, @Nonnull final Expression<?> flag) {
        metadata.addFlag(new QueryFlag(position, flag));
        return self();
    }

    public C addBatch() {
        batches.add(new SQLUpdateBatch(metadata, updates));
        updates = Maps.newLinkedHashMap();
        metadata = new DefaultQueryMetadata();
        metadata.addJoin(JoinType.DEFAULT, entity);
        return self();
    }

    public void clear() {
        batches.clear();
        updates = Maps.newLinkedHashMap();
        metadata = new DefaultQueryMetadata();
        metadata.addJoin(JoinType.DEFAULT, entity);
    }

    @Override
    public <T> C set(@Nonnull final Path<T> path, @Nullable final T value) {
        if (value instanceof Expression<?>) {
            updates.put(path, (Expression<?>) value);
        } else if (value != null) {
            updates.put(path, ConstantImpl.create(value));
        } else {
            setNull(path);
        }
        return self();
    }

    @Override
    public <T> C set(@Nonnull final Path<T> path, @Nullable final Expression<? extends T> expression) {
        if (expression != null) {
            updates.put(path, expression);
        } else {
            setNull(path);
        }
        return self();
    }

    @Override
    public <T> C setNull(@Nonnull final Path<T> path) {
        updates.put(path, Null.CONSTANT);
        return self();
    }

    @Override
    public C set(@Nonnull final List<? extends Path<?>> paths, @Nonnull final List<?> values) {
        for (int i = 0; i < paths.size(); i++) {
            if (values.get(i) instanceof Expression) {
                updates.put(paths.get(i), (Expression<?>) values.get(i));
            } else if (values.get(i) != null) {
                updates.put(paths.get(i), ConstantImpl.create(values.get(i)));
            } else {
                updates.put(paths.get(i), Null.CONSTANT);
            }
        }
        return self();
    }

    public C where(@Nonnull final Predicate p) {
        metadata.addWhere(p);
        return self();
    }

    @Override
    public C where(@Nonnull final Predicate... o) {
        for (Predicate p : o) {
            metadata.addWhere(p);
        }
        return self();
    }

    public C limit(@Nonnegative final long limit) {
        metadata.setModifiers(QueryModifiers.limit(limit));
        return self();
    }

    @Override
    public boolean isEmpty() {
        return updates.isEmpty() && batches.isEmpty();
    }

    public int getBatchCount() {
        return batches.size();
    }

    @Override
    public Mono<Long> execute() {
        if (batches.isEmpty()) {
            return requireConnection()
                    .map(this::createStatement)
                    .flatMap(this::executeStatement);
        } else {
            return requireConnection()
                    .map(this::createStatement)
                    .flatMapMany(this::executeNonBulkBatchStatement)
                    .reduce(0L, Long::sum);
        }
    }

    private Mono<Long> executeStatement(@Nonnull final Statement stmt) {
        return Mono.from(stmt.execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .map(Long::valueOf);
    }

    private Flux<Long> executeNonBulkBatchStatement(Statement stmt) {
        return Flux.from(stmt.execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .map(Long::valueOf);
    }

    private Statement createStatement(@Nonnull final Connection connection) {
        SQLSerializer serializer = createSerializerAndSerialize();
        return prepareStatementAndSetParameters(connection, serializer);
    }

    private Statement prepareStatementAndSetParameters(@Nonnull final Connection connection, @Nonnull final SQLSerializer serializer) {
        String queryString = serializer.toString();
        queryString = R2dbcUtils.replaceBindingArguments(queryString);
        final Statement stmt = connection.createStatement(queryString);
        if (batches.isEmpty()) {
            setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(), metadata.getParams(), 0);
        } else {
            int offset = 0;
            for (SQLUpdateBatch batch : batches) {
                if (useLiterals) {
                    throw new UnsupportedOperationException("Batch updates are not supported with literals");
                }
                setBatchParameters(stmt, batch, offset);
                stmt.add();
            }
        }
        return stmt;
    }

    private void setBatchParameters(@Nonnull final Statement stmt, @Nonnull final SQLUpdateBatch batch, final int offset) {
        final SQLSerializer helperSerializer = createSerializer();
        helperSerializer.serializeUpdate(batch.getMetadata(), entity, batch.getUpdates());
        setParameters(stmt, helperSerializer.getConstants(), helperSerializer.getConstantPaths(), batch.getMetadata().getParams(), offset);
    }


    private SQLSerializer createSerializer() {
        final SQLSerializer serializer = new SQLSerializer(configuration, true);
        serializer.setUseLiterals(useLiterals);
        return serializer;
    }

    private SQLSerializer createSerializerAndSerialize() {
        final SQLSerializer serializer = createSerializer();
        if (!batches.isEmpty()) {
            SQLUpdateBatch first = batches.get(0);
            serializer.serializeUpdate(first.getMetadata(), entity, first.getUpdates());
        } else {
            serializer.serializeUpdate(metadata, entity, updates);
        }
        return serializer;
    }
}
