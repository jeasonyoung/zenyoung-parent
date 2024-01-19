package top.zenyoung.data.r2dbc.querydsl.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QSort;
import org.springframework.data.r2dbc.convert.EntityRowMapper;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.lang.Nullable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import top.zenyoung.data.r2dbc.querydsl.Querydsl;
import top.zenyoung.data.r2dbc.querydsl.QuerydslParameterBinder;
import top.zenyoung.data.r2dbc.querydsl.QuerydslR2dbcPredicateExecutor;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Querydsl 查询处理器
 *
 * @param <M> 数据实体类型
 */
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuerydslR2dbcPredicateExecutorImpl<M> implements QuerydslR2dbcPredicateExecutor<M> {
    private final QBean<M> beanExpression;
    private final RelationalPath<M> path;
    private final SQLQueryFactory queryFactory;
    private final Querydsl querydsl;
    private final DatabaseClient client;
    private final R2dbcConverter converter;
    private final QuerydslParameterBinder querydslParameterBinder;

    @Nonnull
    @Override
    public Mono<M> findOne(@Nonnull final Predicate predicate) {
        var sqlQuery = queryFactory.query()
                .select(beanExpression)
                .where(predicate)
                .from(path);
        return query(sqlQuery).one();
    }

    @Nonnull
    @Override
    public Flux<M> findAll(@Nonnull final Predicate predicate) {
        var query = queryFactory.query()
                .select(beanExpression)
                .from(path)
                .where(predicate);
        return query(query).all();
    }

    @Nonnull
    @Override
    public Flux<M> findAll(@Nonnull final Predicate predicate, @Nonnull final OrderSpecifier<?>... orders) {
        Assert.notNull(predicate, "Predicate must not be null!");
        Assert.notNull(orders, "Order specifiers must not be null!");
        return executeSorted(createQuery(predicate).select(beanExpression), orders);
    }

    @Nonnull
    @Override
    public Flux<M> findAll(@Nonnull final Predicate predicate, @Nonnull final Sort sort) {
        Assert.notNull(predicate, "Predicate must not be null!");
        Assert.notNull(sort, "Sort must not be null!");
        return executeSorted(createQuery(predicate).select(beanExpression), sort);
    }

    @Nonnull
    @Override
    public Flux<M> findAll(@Nonnull final OrderSpecifier<?>... orders) {
        Assert.notNull(orders, "Order specifiers must not be null!");
        return executeSorted(createQuery().select(beanExpression), orders);
    }

    @Nonnull
    @Override
    public Mono<Long> count(@Nonnull final Predicate predicate) {
        var sqlQuery = queryFactory.query()
                .select(Wildcard.count)
                .from(path)
                .where(predicate);
        return query(sqlQuery).one();
    }

    @Nonnull
    @Override
    public Mono<Boolean> exists(@Nonnull final Predicate predicate) {
        return count(predicate).map(result -> result > 0);
    }

    @Nonnull
    @Override
    public <S extends M, R, P extends Publisher<R>> P findBy(@Nonnull final Predicate predicate,
                                                             @Nonnull Function<FluentQuery.ReactiveFluentQuery<S>, P> queryFunction) {
        throw new UnsupportedOperationException();
    }

    protected SQLQuery<?> createQuery(@Nullable final Predicate... predicate) {
        var query = querydsl.createQuery(path);
        if (predicate != null) {
            query = query.where(predicate);
        }
        return query;
    }

    @Nonnull
    private Flux<M> executeSorted(@Nonnull final SQLQuery<M> query, @Nonnull final OrderSpecifier<?>... orders) {
        return executeSorted(query, new QSort(orders));
    }

    @Nonnull
    private Flux<M> executeSorted(@Nonnull final SQLQuery<M> query, @Nonnull final Sort sort) {
        var sqlQuery = querydsl.applySorting(sort, query);
        return query(sqlQuery).all();
    }

    private <O> RowsFetchSpec<O> query(@Nonnull final SQLQuery<O> query) {
        var mapper = new EntityRowMapper<>(query.getType(), converter);
        var sql = query.getSQL();
        return querydslParameterBinder.bind(client, sql.getNullFriendlyBindings(), sql.getSQL()).map(mapper);
    }

    @Nonnull
    @Override
    public Flux<M> findAll(@Nonnull final Predicate predicate, @Nonnull final Pageable pageable) {
        SQLQuery<M> sqlQuery = createQuery(predicate).select(beanExpression);
        sqlQuery = querydsl.applyPagination(pageable, sqlQuery);
        return query(sqlQuery).all();
    }
}
