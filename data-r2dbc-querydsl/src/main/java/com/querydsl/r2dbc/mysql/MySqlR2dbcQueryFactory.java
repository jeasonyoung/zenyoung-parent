package com.querydsl.r2dbc.mysql;

import com.querydsl.core.QueryFlag;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.r2dbc.AbstractR2dbcQueryFactory;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.dml.R2dbcInsertClause;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;

import javax.annotation.Nonnull;

public class MySqlR2dbcQueryFactory extends AbstractR2dbcQueryFactory<MySqlR2dbcQuery<?>> {
    public MySqlR2dbcQueryFactory(@Nonnull final R2dbcConnectionProvider provider) {
        this(provider, new Configuration(new MySQLTemplates()));
    }

    public MySqlR2dbcQueryFactory(@Nonnull final R2dbcConnectionProvider provider, @Nonnull final SQLTemplates templates) {
        this(provider, new Configuration(templates));
    }

    public MySqlR2dbcQueryFactory(@Nonnull final R2dbcConnectionProvider provider, @Nonnull final Configuration configuration) {
        super(provider, configuration);
    }

    public R2dbcInsertClause insertIgnore(@Nonnull final EntityPath<?> entity) {
        final R2dbcInsertClause insert = insert(entity);
        insert.addFlag(QueryFlag.Position.START_OVERRIDE, "insert ignore into ");
        return insert;
    }

    public R2dbcInsertClause insertOnDuplicateKeyUpdate(@Nonnull final EntityPath<?> entity, @Nonnull final String clause) {
        final R2dbcInsertClause insert = insert(entity);
        insert.addFlag(QueryFlag.Position.END, " on duplicate key update " + clause);
        return insert;
    }

    public R2dbcInsertClause insertOnDuplicateKeyUpdate(@Nonnull final EntityPath<?> entity, @Nonnull final Expression<?> clause) {
        final R2dbcInsertClause insert = insert(entity);
        insert.addFlag(QueryFlag.Position.END, ExpressionUtils.template(String.class, " on duplicate key update {0}", clause));
        return insert;
    }

    public R2dbcInsertClause insertOnDuplicateKeyUpdate(@Nonnull final EntityPath<?> entity, @Nonnull final Expression<?>... clauses) {
        final R2dbcInsertClause insert = insert(entity);
        final StringBuilder flag = new StringBuilder(" on duplicate key update ");
        for (int i = 0; i < clauses.length; i++) {
            flag.append(i > 0 ? ", " : "").append("{").append(i).append("}");
        }
        insert.addFlag(QueryFlag.Position.END, ExpressionUtils.template(String.class, flag.toString(), (Object[]) clauses));
        return insert;
    }

    @Override
    public MySqlR2dbcQuery<?> query() {
        return new MySqlR2dbcQuery<>(provider, configuration);
    }

    public MySqlR2dbcReplaceClause replace(@Nonnull final EntityPath<?> entity) {
        return new MySqlR2dbcReplaceClause(provider, configuration, entity);
    }

    @Override
    public <T> MySqlR2dbcQuery<T> select(@Nonnull final Expression<T> expr) {
        return query().select(expr);
    }

    @Override
    public MySqlR2dbcQuery<Tuple> select(@Nonnull final Expression<?>... exprs) {
        return query().select(exprs);
    }

    @Override
    public <T> MySqlR2dbcQuery<T> selectDistinct(@Nonnull final Expression<T> expr) {
        return query().select(expr).distinct();
    }

    @Override
    public MySqlR2dbcQuery<Tuple> selectDistinct(@Nonnull final Expression<?>... exprs) {
        return query().select(exprs).distinct();
    }

    @Override
    public MySqlR2dbcQuery<Integer> selectZero() {
        return select(Expressions.ZERO);
    }

    @Override
    public MySqlR2dbcQuery<Integer> selectOne() {
        return select(Expressions.ONE);
    }

    @Override
    public <T> MySqlR2dbcQuery<T> selectFrom(@Nonnull final EntityPath<T> expr) {
        return select(expr).from(expr);
    }
}
