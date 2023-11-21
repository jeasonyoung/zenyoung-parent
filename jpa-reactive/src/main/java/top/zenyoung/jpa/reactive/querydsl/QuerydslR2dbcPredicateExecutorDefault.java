package top.zenyoung.jpa.reactive.querydsl;

import com.google.common.collect.Lists;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.r2dbc.mysql.MySqlR2dbcQuery;
import com.querydsl.r2dbc.mysql.MySqlR2dbcQueryFactory;
import com.querydsl.sql.RelationalPath;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.function.Function;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuerydslR2dbcPredicateExecutorDefault<M> implements QuerydslR2dbcPredicateExecutor<M> {
    private final ConstructorExpression<M> constructorExpression;
    private final RelationalPath<M> path;
    private final MySqlR2dbcQueryFactory queryFactory;
    private final QuerydslR2dbc querydsl;

    @Nonnull
    @Override
    public Mono<M> findOne(@Nonnull final Predicate predicate) {
        return queryFactory.query()
                .select(constructorExpression)
                .from(path)
                .where(predicate)
                .fetchOne();
    }

    @Nonnull
    @Override
    public Flux<M> findAll(@Nonnull final Predicate predicate) {
        return queryFactory.query()
                .select(constructorExpression)
                .from(path)
                .fetch();
    }

    @Nonnull
    @Override
    public Flux<M> findAll(@Nonnull final Predicate predicate, @Nonnull final Sort sort) {
        MySqlR2dbcQuery<M> query = queryFactory.query()
                .select(constructorExpression)
                .from(path)
                .where(predicate);
        if (sort.isUnsorted()) {
            return query.fetch();
        }
        query = querydsl.applySorting(sort, query);
        return query.fetch();
    }

    @Nonnull
    @Override
    public Flux<M> findAll(@Nonnull final Predicate predicate, @Nonnull final OrderSpecifier<?>... orders) {
        MySqlR2dbcQuery<M> query = queryFactory.query()
                .select(constructorExpression)
                .from(path)
                .where(predicate);
        if (orders.length > 0) {
            query = query.orderBy(orders);
        }
        return query.fetch();
    }

    @Nonnull
    @Override
    public Flux<M> findAll(@Nonnull final OrderSpecifier<?>... orders) {
        return queryFactory.query()
                .select(constructorExpression)
                .from(path)
                .orderBy(orders)
                .fetch();
    }

    @Override
    public Mono<Page<M>> findAll(@Nonnull final Predicate predicate, @Nonnull final Pageable pageable) {
        return count(predicate)
                .flatMap(count -> {
                    if (count == 0) {
                        return Mono.just(PageableExecutionUtils.getPage(Lists.newArrayList(), pageable, () -> count));
                    }
                    return querydsl.applyPagination(pageable, queryFactory.query())
                            .select(constructorExpression)
                            .from(path)
                            .where(predicate)
                            .fetch()
                            .collectList()
                            .map(rows -> PageableExecutionUtils.getPage(rows, pageable, () -> count));
                });
    }

    @Nonnull
    @Override
    public Mono<Long> count(@Nonnull final Predicate predicate) {
        final NumberExpression<Long> count = ((SimpleExpression<?>) constructorExpression.getArgs().get(0)).count();
        return queryFactory.query()
                .select(count)
                .from(path)
                .where(predicate)
                .fetchOne();
    }

    @Nonnull
    @Override
    public Mono<Boolean> exists(@Nonnull final Predicate predicate) {
        return this.count(predicate).map(ret -> ret > 0);
    }

    @Nonnull
    @Override
    public <S extends M, R, P extends Publisher<R>> P findBy(@Nonnull final Predicate predicate,
                                                             @Nonnull final Function<FluentQuery.ReactiveFluentQuery<S>, P> queryFunction) {
        throw new UnsupportedOperationException();
    }
}
