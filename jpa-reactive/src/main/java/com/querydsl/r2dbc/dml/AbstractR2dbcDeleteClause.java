package com.querydsl.r2dbc.dml;

import com.google.common.collect.Lists;
import com.querydsl.core.*;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.ValidatingVisitor;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.core.dml.DeleteClause;
import com.querydsl.r2dbc.core.internal.R2dbcUtils;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializer;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;

public abstract class AbstractR2dbcDeleteClause<C extends AbstractR2dbcDeleteClause<C>>
        extends AbstractR2dbcClause<C> implements DeleteClause<C> {
    private static final ValidatingVisitor validatingVisitor = new ValidatingVisitor("Undeclared path '%s'. " +
            "A delete operation can only reference a single table. " +
            "Consider this alternative: DELETE ... WHERE EXISTS (subquery)");
    private final RelationalPath<?> entity;
    private final List<QueryMetadata> batches = Lists.newArrayList();
    private DefaultQueryMetadata metadata = new DefaultQueryMetadata();

    public AbstractR2dbcDeleteClause(@Nonnull final R2dbcConnectionProvider provider,
                                     @Nonnull final Configuration configuration,
                                     @Nonnull final RelationalPath<?> entity) {
        super(provider, configuration);
        this.entity = entity;
        metadata.addJoin(JoinType.DEFAULT, entity);
        metadata.setValidatingVisitor(validatingVisitor);
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

    public C addFlag(@Nonnull QueryFlag.Position position, @Nonnull final Expression<?> flag) {
        metadata.addFlag(new QueryFlag(position, flag));
        return self();
    }

    public C addBatch() {
        batches.add(metadata);
        metadata = new DefaultQueryMetadata();
        metadata.addJoin(JoinType.DEFAULT, entity);
        metadata.setValidatingVisitor(validatingVisitor);
        return self();
    }

    public void clear() {
        batches.clear();
        metadata = new DefaultQueryMetadata();
        metadata.addJoin(JoinType.DEFAULT, entity);
        metadata.setValidatingVisitor(validatingVisitor);
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

    public int getBatchCount() {
        return batches.size();
    }

    private SQLSerializer createSerializer() {
        final SQLSerializer serializer = new SQLSerializer(configuration, true);
        serializer.setUseLiterals(useLiterals);
        return serializer;
    }

    private SQLSerializer createSerializerAndSerialize() {
        final SQLSerializer serializer = createSerializer();
        if (!batches.isEmpty()) {
            final QueryMetadata first = batches.get(0);
            serializer.serializeDelete(first, entity);
        } else {
            serializer.serializeDelete(metadata, entity);
        }
        return serializer;
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

    private Mono<Long> executeStatement(Statement stmt) {
        return Mono.from(stmt.execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .map(Long::valueOf);
    }

    private Flux<Long> executeNonBulkBatchStatement(@Nonnull final Statement stmt) {
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
        Statement stmt = connection.createStatement(queryString);
        if (batches.isEmpty()) {
            setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(), metadata.getParams(), 0);
        } else {
            int offset = 0;
            for (QueryMetadata batch : batches) {
                if (useLiterals) {
                    throw new UnsupportedOperationException("Batch deletes are not supported with literals");
                }
                setBatchParameters(stmt, batch, offset);
                stmt.add();
            }
        }
        return stmt;
    }

    private void setBatchParameters(@Nonnull final Statement stmt, @Nonnull final QueryMetadata batch, final int offset) {
        final SQLSerializer helperSerializer = createSerializer();
        helperSerializer.serializeDelete(batch, entity);
        setParameters(stmt, helperSerializer.getConstants(), helperSerializer.getConstantPaths(), metadata.getParams(), offset);
    }
}
