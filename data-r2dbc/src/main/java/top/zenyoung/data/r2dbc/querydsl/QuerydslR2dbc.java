package top.zenyoung.data.r2dbc.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.r2dbc.mysql.MySqlR2dbcQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QSort;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.List;

@RequiredArgsConstructor
public class QuerydslR2dbc {
    private final RelationalPersistentEntity<?> entity;

    public <T> MySqlR2dbcQuery<T> applyPagination(@Nonnull final Pageable pageable, @Nonnull final MySqlR2dbcQuery<T> query) {
        Assert.notNull(pageable, "Pageable must not be null!");
        Assert.notNull(query, "SQLQuery must not be null!");
        if (pageable.isUnpaged()) {
            return query;
        }

        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

        return applySorting(pageable.getSort(), query);
    }

    public <T> MySqlR2dbcQuery<T> applySorting(@Nonnull final Sort sort, @Nonnull final MySqlR2dbcQuery<T> query) {
        if (sort.isUnsorted()) {
            return query;
        }
        if (sort instanceof QSort qSort) {
            return addOrderByFrom(qSort, query);
        }
        return addOrderByFrom(sort, query);
    }

    private <T> MySqlR2dbcQuery<T> addOrderByFrom(@Nonnull final QSort qSort, @Nonnull final MySqlR2dbcQuery<T> query) {
        final List<OrderSpecifier<?>> orderSpecifiers = qSort.getOrderSpecifiers();
        return query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]));
    }

    private <T> MySqlR2dbcQuery<T> addOrderByFrom(@Nonnull final Sort sort, @Nonnull final MySqlR2dbcQuery<T> query) {
        MySqlR2dbcQuery<T> q = query;
        for (final Sort.Order order : sort) {
            q = query.orderBy(toOrderSpecifier(order));
        }
        return q;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private OrderSpecifier<?> toOrderSpecifier(@Nonnull final Sort.Order order) {
        return new OrderSpecifier(
                order.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC,
                buildOrderPropertyPathFrom(order),
                toQueryDslNullHandling(order.getNullHandling())
        );
    }

    private Expression<?> buildOrderPropertyPathFrom(@Nonnull final Sort.Order order) {
        final RelationalPersistentProperty persistentProperty = entity.getRequiredPersistentProperty(order.getProperty());
        final String columnName = persistentProperty.getColumnName().getReference();
        return Expressions.stringPath(columnName);
    }

    private OrderSpecifier.NullHandling toQueryDslNullHandling(@Nonnull final Sort.NullHandling nullHandling) {
        Assert.notNull(nullHandling, "NullHandling must not be null!");
        if (nullHandling == Sort.NullHandling.NULLS_FIRST) {
            return OrderSpecifier.NullHandling.NullsFirst;
        }
        if (nullHandling == Sort.NullHandling.NULLS_LAST) {
            return OrderSpecifier.NullHandling.NullsLast;
        }
        return OrderSpecifier.NullHandling.Default;
    }
}
