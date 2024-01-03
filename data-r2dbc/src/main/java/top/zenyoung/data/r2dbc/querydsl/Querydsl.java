package top.zenyoung.data.r2dbc.querydsl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.OrderSpecifier.NullHandling;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.querydsl.QSort;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;

/**
 * Querydsl 工具类
 *
 * @author young
 */
@RequiredArgsConstructor(staticName = "of")
public class Querydsl {
    private final SQLQueryFactory sqlQueryFactory;
    private final RelationalPersistentEntity<?> entity;

    /**
     * 创建查询处理器
     *
     * @return 查询处理器
     */
    public SQLQuery<?> createQuery() {
        return sqlQueryFactory.query();
    }

    /**
     * 创建查询处理器
     *
     * @param paths 查询实体
     * @return 查询处理器
     */
    public SQLQuery<?> createQuery(@Nonnull final EntityPath<?>... paths) {
        Assert.notNull(paths, "Paths must not be null!");
        return createQuery().from(paths);
    }

    /**
     * 处理分页查询器
     *
     * @param pageable 分页器
     * @param query    查询条件
     * @param <T>      数据实体类型
     * @return 查询结果
     */
    public <T> SQLQuery<T> applyPagination(@Nonnull final Pageable pageable, @Nonnull final SQLQuery<T> query) {
        Assert.notNull(pageable, "Pageable must not be null!");
        Assert.notNull(query, "SQLQuery must not be null!");
        if (pageable.isUnpaged()) {
            return query;
        }
        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());
        return applySorting(pageable.getSort(), query);
    }

    /**
     * 处理排序查询器
     *
     * @param sort  排序器
     * @param query 查询器
     * @param <T>   数据实体类型
     * @return 查询结果
     */
    public <T> SQLQuery<T> applySorting(@Nonnull final Sort sort, @Nonnull final SQLQuery<T> query) {
        Assert.notNull(sort, "Sort must not be null!");
        Assert.notNull(query, "Query must not be null!");
        if (sort.isUnsorted()) {
            return query;
        }
        if (sort instanceof QSort qSort) {
            return addOrderByFrom(qSort, query);
        }
        return addOrderByFrom(sort, query);
    }

    private <T> SQLQuery<T> addOrderByFrom(@Nonnull final QSort qsort, @Nonnull final SQLQuery<T> query) {
        var orderSpecifiers = qsort.getOrderSpecifiers();
        return query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]));
    }

    private <T> SQLQuery<T> addOrderByFrom(@Nonnull final Sort sort, @Nonnull final SQLQuery<T> query) {
        Assert.notNull(sort, "Sort must not be null!");
        Assert.notNull(query, "Query must not be null!");
        for (var order : sort) {
            query.orderBy(toOrderSpecifier(order));
        }
        return query;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private OrderSpecifier<?> toOrderSpecifier(@Nonnull final Order order) {
        return new OrderSpecifier(
                order.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC,
                buildOrderPropertyPathFrom(order), toQueryDslNullHandling(order.getNullHandling()));
    }

    private NullHandling toQueryDslNullHandling(@Nonnull final Sort.NullHandling nullHandling) {
        Assert.notNull(nullHandling, "NullHandling must not be null!");
        return switch (nullHandling) {
            case NULLS_FIRST -> NullHandling.NullsFirst;
            case NULLS_LAST -> NullHandling.NullsLast;
            case NATIVE -> NullHandling.Default;
        };
    }

    private Expression<?> buildOrderPropertyPathFrom(@Nonnull final Order order) {
        Assert.notNull(order, "Order must not be null!");
        var persistentProperty = entity.getRequiredPersistentProperty(order.getProperty());
        var columnName = persistentProperty.getColumnName().getReference();
        return Expressions.stringPath(columnName);
    }
}
