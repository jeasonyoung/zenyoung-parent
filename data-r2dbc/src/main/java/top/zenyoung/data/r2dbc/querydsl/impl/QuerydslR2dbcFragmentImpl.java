package top.zenyoung.data.r2dbc.querydsl.impl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.QBean;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLUpdateClause;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.convert.EntityRowMapper;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import top.zenyoung.data.r2dbc.querydsl.QuerydslParameterBinder;
import top.zenyoung.data.r2dbc.querydsl.QuerydslR2dbcFragment;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * R2dbc Fragment 查询接口实现
 *
 * @param <T> 数据实体类型
 * @author young
 */
@RequiredArgsConstructor
public class QuerydslR2dbcFragmentImpl<T> implements QuerydslR2dbcFragment<T> {
    private final SQLQueryFactory queryFactory;
    private final QBean<T> beanExpression;
    private final RelationalPath<T> path;
    private final DatabaseClient client;
    private final R2dbcConverter converter;
    private final QuerydslParameterBinder querydslParameterBinder;

    @Override
    public <O> RowsFetchSpec<O> query(@Nonnull final Function<SQLQuery<?>, SQLQuery<O>> query) {
        return createQuery(query);
    }

    @Override
    @Transactional
    public Mono<Long> update(@Nonnull final UnaryOperator<SQLUpdateClause> update) {
        var clause = queryFactory.update(path);
        var sqlBindings = update.apply(clause).getSQL();
        var bindings = getBindings(sqlBindings);
        var sql = sqlBindings.stream()
                .map(SQLBindings::getSQL)
                .collect(Collectors.joining("\n"));
        return querydslParameterBinder.bind(client, bindings, sql)
                .fetch()
                .rowsUpdated();
    }

    @Override
    @Transactional
    public Mono<Long> deleteWhere(@Nonnull final Predicate predicate) {
        var clause = queryFactory.delete(path).where(predicate);
        var sqlBindings = clause.getSQL();
        var bindings = getBindings(sqlBindings);
        var sql = sqlBindings.stream()
                .map(SQLBindings::getSQL)
                .collect(Collectors.joining("\n"));
        return querydslParameterBinder.bind(client, bindings, sql)
                .fetch()
                .rowsUpdated();
    }

    @Override
    public Expression<T> entityProjection() {
        return beanExpression;
    }

    private <O> RowsFetchSpec<O> createQuery(@Nonnull final Function<SQLQuery<?>, SQLQuery<O>> query) {
        final var result = query.apply(queryFactory.query());
        final var mapper = new EntityRowMapper<>(result.getType(), converter);
        final var sql = result.getSQL();
        return querydslParameterBinder.bind(client, sql.getNullFriendlyBindings(), sql.getSQL()).map(mapper);
    }

    private List<Object> getBindings(@Nonnull final List<SQLBindings> sqlBindings) {
        return sqlBindings.stream()
                .flatMap(bindings -> bindings.getNullFriendlyBindings().stream())
                .toList();
    }
}
