package com.querydsl.r2dbc;

import com.querydsl.core.types.*;
import com.querydsl.sql.SQLOps;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.util.List;

@UtilityClass
public class UnionUtils {
    public static <T> Expression<T> union(@Nonnull final List<SubQueryExpression<T>> union, final boolean unionAll) {
        final Operator operator = unionAll ? SQLOps.UNION_ALL : SQLOps.UNION;
        Expression<T> rv = union.get(0);
        for (int i = 1; i < union.size(); i++) {
            rv = ExpressionUtils.operation(rv.getType(), operator, rv, union.get(i));
        }
        return rv;
    }

    public static <T> Expression<T> union(@Nonnull final List<SubQueryExpression<T>> union, @Nonnull final Path<T> alias, final boolean unionAll) {
        final Expression<T> rv = union(union, unionAll);
        return ExpressionUtils.as(rv, alias);
    }
}
