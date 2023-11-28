package com.querydsl.r2dbc.core.internal;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;

@UtilityClass
public class R2dbcUtils {
    public static String replaceBindingArguments(@Nonnull final String originalSql) {
        String sql = originalSql;
        int counter = 1;
        for (; ; ) {
            int index = sql.indexOf('?');
            if (index == -1) {
                break;
            }
            final String first = sql.substring(0, index);
            final String second = sql.substring(index + 1);
            sql = first + "$" + counter + second;
            counter++;
        }
        return sql;
    }
}
