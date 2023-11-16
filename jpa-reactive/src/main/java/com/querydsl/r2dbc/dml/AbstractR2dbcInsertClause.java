package com.querydsl.r2dbc.dml;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.JoinType;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.*;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.core.dml.InsertClause;
import com.querydsl.r2dbc.core.internal.R2dbcUtils;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.dml.SQLInsertBatch;
import com.querydsl.sql.types.Null;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractR2dbcInsertClause<C extends AbstractR2dbcInsertClause<C>>
        extends AbstractR2dbcClause<C> implements InsertClause<C> {
    private final List<SQLInsertBatch> batches = Lists.newArrayList();
    private final List<Path<?>> columns = Lists.newArrayList();
    private final List<Expression<?>> values = Lists.newArrayList();
    private final RelationalPath<?> entity;
    private final QueryMetadata metadata = new DefaultQueryMetadata();
    @Nullable
    private SubQueryExpression<?> subQuery;
    private transient boolean batchToBulk;

    public AbstractR2dbcInsertClause(@Nonnull final R2dbcConnectionProvider provider,
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

    public C withBatchToBulk() {
        setBatchToBulk(true);
        return self();
    }

    public void setBatchToBulk(boolean b) {
        this.batchToBulk = b && configuration.getTemplates().isBatchToBulkSupported();
    }

    private SQLSerializer createSerializer() {
        final SQLSerializer serializer = new SQLSerializer(configuration, true);
        serializer.setUseLiterals(useLiterals);
        return serializer;
    }

    private SQLSerializer createSerializerAndSerialize() {
        final SQLSerializer serializer = createSerializer();
        if (!batches.isEmpty() && batchToBulk) {
            serializer.serializeInsert(metadata, entity, batches);
        } else if (!batches.isEmpty()) {
            SQLInsertBatch first = batches.get(0);
            serializer.serializeInsert(metadata, entity, first.getColumns(), first.getValues(), subQuery);
        } else {
            serializer.serializeInsert(metadata, entity, columns, values, subQuery);
        }
        return serializer;
    }

    public void clear() {
        batches.clear();
        columns.clear();
        values.clear();
        subQuery = null;
    }

    @Override
    public C columns(@Nonnull final Path<?>... columns) {
        this.columns.addAll(Lists.newArrayList(columns));
        return self();
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public C select(@Nonnull final SubQueryExpression<?> sq) {
        this.subQuery = sq;
        for (Map.Entry<ParamExpression<?>, Object> entry : sq.getMetadata().getParams().entrySet()) {
            final ParamExpression<Object> key = (ParamExpression<Object>) entry.getKey();
            metadata.setParam(key, entry.getValue());
        }
        return self();
    }

    @Override
    public C values(@Nonnull final Object... v) {
        for (Object value : v) {
            if (value instanceof Expression<?>) {
                values.add((Expression<?>) value);
            } else if (value != null) {
                values.add(ConstantImpl.create(value));
            } else {
                values.add(Null.CONSTANT);
            }
        }
        return self();
    }

    @Override
    public <T> C set(@Nonnull final Path<T> path, @Nullable final T value) {
        columns.add(path);
        if (value instanceof Expression<?>) {
            values.add((Expression<?>) value);
        } else if (value != null) {
            values.add(ConstantImpl.create(value));
        } else {
            values.add(Null.CONSTANT);
        }
        return self();
    }

    @Override
    public <T> C set(@Nonnull final Path<T> path, @Nonnull final Expression<? extends T> expression) {
        columns.add(path);
        values.add(expression);
        return self();
    }

    @Override
    public <T> C setNull(@Nonnull final Path<T> path) {
        columns.add(path);
        values.add(Null.CONSTANT);
        return self();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty() && batches.isEmpty();
    }

    public C addBatch() {
        batches.add(new SQLInsertBatch(columns, values, subQuery));
        columns.clear();
        values.clear();
        subQuery = null;
        return self();
    }

    @SuppressWarnings("unchecked")
    public <T> Mono<T> executeWithKey(@Nonnull final Path<T> path) {
        final Class<T> type = (Class<T>) path.getType();
        final Mapper<T> mapper = (row, metadata) -> Objects.requireNonNull(row.get(0, type), "Null key result");
        return requireConnection()
                .map(connection -> createStatement(connection, true))
                .flatMap(connection -> executeStatementWithKey(connection, mapper));
    }

    private <T> Mono<T> executeStatementWithKey(@Nonnull final Statement stmt, @Nonnull final Mapper<T> mapper) {
        return Mono.from(stmt.execute())
                .flatMap(result -> Mono.from(result.map(mapper::map)));
    }

    public <T> Flux<T> executeWithKeys(@Nonnull final Path<T> path) {
        final Mapper<T> mapper = (row, metadata) -> Objects.requireNonNull(row.get(0, path.getType()), "Null key result");
        return requireConnection()
                .map(connection -> createStatement(connection, true))
                .flatMapMany(connection -> executeStatementWithKeys(connection, mapper));
    }

    @Override
    public Mono<Long> execute() {
        if (batchToBulk || batches.isEmpty()) {
            return requireConnection()
                    .map(connection -> createStatement(connection, false))
                    .flatMap(this::executeStatement);
        } else {
            return requireConnection()
                    .map(connection -> createStatement(connection, false))
                    .flatMapMany(this::executeNonBulkBatchStatement)
                    .reduce(0L, Long::sum);
        }
    }

    private Mono<Long> executeStatement(@Nonnull final Statement stmt) {
        return Mono.from(stmt.execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .map(Long::valueOf);
    }

    private <T> Flux<T> executeStatementWithKeys(@Nonnull final Statement stmt, @Nonnull final Mapper<T> mapper) {
        return Flux.from(stmt.execute())
                .flatMap(result -> Mono.from(result.map(mapper::map)));
    }

    private Flux<Long> executeNonBulkBatchStatement(@Nonnull final Statement stmt) {
        return Flux.from(stmt.execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .map(Long::valueOf);
    }

    private Statement createStatement(@Nonnull final Connection connection, final boolean withKeys) {
        final SQLSerializer serializer = createSerializerAndSerialize();
        return prepareStatementAndSetParameters(connection, serializer, withKeys);
    }

    private Statement prepareStatementAndSetParameters(@Nonnull final Connection connection, @Nonnull final SQLSerializer serializer, final boolean withKeys) {
        String queryString = serializer.toString();
        queryString = R2dbcUtils.replaceBindingArguments(queryString);
        final Statement stmt = connection.createStatement(queryString);
        if (batches.isEmpty()) {
            setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(), metadata.getParams(), 0);
        } else {
            int offset = 0;
            for (SQLInsertBatch batch : batches) {
                if (useLiterals) {
                    throw new UnsupportedOperationException("Batch inserts are not supported with literals");
                }
                setBatchParameters(stmt, batch, offset);
                if (!batchToBulk) {
                    stmt.add();
                } else {
                    offset++;
                }
            }
        }
        if (withKeys && (entity.getPrimaryKey() != null)) {
            final String[] target = new String[entity.getPrimaryKey().getLocalColumns().size()];
            for (int i = 0; i < target.length; i++) {
                final Path<?> path = entity.getPrimaryKey().getLocalColumns().get(i);
                final String column = ColumnMetadata.getName(path);
                target[i] = configuration.getTemplates().quoteIdentifier(column);
            }
            stmt.returnGeneratedValues(target);
        }
        return stmt;
    }

    @SuppressWarnings({"unchecked"})
    private <T> void setBatchParameters(@Nonnull final Statement stmt, @Nonnull final SQLInsertBatch batch, final int offset) {
        final Map<ParamExpression<?>, Object> params = Maps.newHashMap();
        final List<Object> constants = batch.getValues()
                .stream()
                .map(c -> ((Constant<T>) c).getConstant())
                .collect(Collectors.toList());
        setParameters(stmt, constants, batch.getColumns(), params, offset);
    }

    @FunctionalInterface
    private interface Mapper<T> {
        @Nonnull
        T map(@Nonnull final Row row, @Nonnull final RowMetadata metadata);
    }
}
