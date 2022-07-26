package top.zenyoung.orm.util;

import com.baomidou.mybatisplus.core.conditions.AbstractLambdaWrapper;
import com.baomidou.mybatisplus.core.conditions.ISqlSegment;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import top.zenyoung.common.util.MapUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MybatisPlus工具类
 *
 * @author young
 */
@Slf4j
public class MybatisPlusUtils {

    private static <T> Map<String, Object> from(@Nonnull final T dto) {
        final Map<String, Object> dtoMap = MapUtils.from(dto);
        if (CollectionUtils.isEmpty(dtoMap)) {
            return Maps.newHashMap();
        }
        return dtoMap.entrySet().stream()
                .map(entry -> {
                    final String key = entry.getKey();
                    final Object val = entry.getValue();
                    if (!Strings.isNullOrEmpty(key) && Objects.nonNull(val)) {
                        if (val instanceof String) {
                            String text = (String) val;
                            if (Strings.isNullOrEmpty(text)) {
                                return null;
                            }
                            text = text.replaceAll(Constants.QUOTE, Constants.EMPTY).trim();
                            if (Strings.isNullOrEmpty(text)) {
                                return null;
                            }
                        }
                        return Pair.of(key, val);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight, (o, n) -> n));

    }

    @SuppressWarnings({"unchecked"})
    private static <R> void buildFieldMap(@Nonnull final Map<String, Object> params, @Nonnull final Class<R> cls,
                                          @Nonnull final AbstractLambdaWrapper<R, ?> lambdaWrapper, @Nullable final List<String> excludes) {
        if (!CollectionUtils.isEmpty(params)) {
            final Map<String, ColumnCache> colCacheMap = LambdaUtils.getColumnMap(cls);
            if (!CollectionUtils.isEmpty(colCacheMap)) {
                final boolean isUpdate = (lambdaWrapper instanceof LambdaUpdateWrapper);
                final Map<String, Method> methodMap = Stream.of(ReflectionUtils.getAllDeclaredMethods(lambdaWrapper.getClass()))
                        .collect(Collectors.toMap(Method::getName, Function.identity(), (n, o) -> n));
                params.forEach((name, val) -> {
                    final ColumnCache colCache = colCacheMap.get(LambdaUtils.formatKey(name));
                    if (Objects.nonNull(colCache) && Objects.nonNull(val)) {
                        //检查是否排除字段
                        if (!CollectionUtils.isEmpty(excludes) && excludes.contains(name)) {
                            return;
                        }
                        final String colName = colCache.getColumn();
                        if (isUpdate) {
                            //更新处理
                            buildUpdateFieldMap((LambdaUpdateWrapper<R>) lambdaWrapper, methodMap, colName, val);
                        } else {
                            //查询处理
                            buildQueryFieldMap((LambdaQueryWrapper<R>) lambdaWrapper, methodMap, colName, val);
                        }
                    }
                });
            }
        }
    }

    private static <R> String formatSqlHandler(@Nonnull final AbstractLambdaWrapper<R, ?> queryWrapper, @Nonnull final Method method, @Nonnull final Object... params) {
        try {
            method.setAccessible(true);
            return (String) method.invoke(queryWrapper, "{0}", params);
        } catch (Throwable e) {
            log.warn("formatSqlHandler(params: {})-exp: {}", params, e.getMessage());
        }
        return null;
    }

    private static <R> void buildQueryFieldMap(@Nonnull final LambdaQueryWrapper<R> queryWrapper, @Nonnull final Map<String, Method> methodMap,
                                               @Nonnull final String colName, @Nonnull final Object val) {
        final Method method = methodMap.get("doIt"), formatSqlMethod = methodMap.get("formatSql");
        if (Objects.nonNull(method) && Objects.nonNull(formatSqlMethod)) {
            try {
                final String formatSql = formatSqlHandler(queryWrapper, formatSqlMethod, val);
                if (!Strings.isNullOrEmpty(formatSql)) {
                    final SqlKeyword eq = SqlKeyword.EQ;
                    final ISqlSegment col = () -> colName;
                    final ISqlSegment colVal = () -> formatSql;
                    method.setAccessible(true);
                    method.invoke(queryWrapper, true, new ISqlSegment[]{col, eq, colVal});
                }
            } catch (Throwable e) {
                log.warn("buildQueryFieldMap(queryWrapper: {},colCache: {})-exp: {}", queryWrapper, colName, e.getMessage());
            }
        }
    }

    private static <R> void buildUpdateFieldMap(@Nonnull final LambdaUpdateWrapper<R> updateWrapper, @Nonnull final Map<String, Method> methodMap,
                                                @Nonnull final String colName, @Nonnull final Object val) {
        final Method formatSqlMethod = methodMap.get("formatSql");
        if (Objects.nonNull(formatSqlMethod)) {
            try {
                final String formatSql = formatSqlHandler(updateWrapper, formatSqlMethod, val);
                if (!Strings.isNullOrEmpty(formatSql)) {
                    updateWrapper.setSql(true, String.format("%s=%s", colName, formatSql));
                }
            } catch (Throwable e) {
                log.warn("buildUpdateFieldMap(updateWrapper: {},colName: {},val: {})-exp: {}", updateWrapper, colName, val, e.getMessage());
            }
        }
    }

    public static <R> LambdaQueryWrapper<R> buildQueryWrapper(@Nonnull final Map<String, Object> params, @Nonnull final Class<R> cls, @Nullable final List<String> excludes) {
        final LambdaQueryWrapper<R> queryWrapper = Wrappers.lambdaQuery(cls);
        if (!CollectionUtils.isEmpty(params)) {
            buildFieldMap(params, cls, queryWrapper, excludes);
        }
        return queryWrapper;
    }

    public static <T, R> LambdaQueryWrapper<R> buildQueryWrapper(@Nonnull final T dto, @Nonnull final Class<R> clazz, @Nullable final List<String> excludes) {
        final Map<String, Object> args = from(dto);
        return buildQueryWrapper(args, clazz, excludes);
    }

    public static <T, R> LambdaQueryWrapper<R> buildQueryWrapper(@Nonnull final T dto, @Nonnull final Class<R> clazz) {
        return buildQueryWrapper(dto, clazz, null);
    }

    public static <T, R> LambdaQueryWrapper<R> buildQueryWrapper(@Nonnull final Map<String, Object> params, @Nonnull final Class<R> clazz) {
        return buildQueryWrapper(params, clazz, null);
    }

    public static <T, R> LambdaUpdateWrapper<R> buildUpdateWrapper(@Nonnull final T dto, @Nonnull final Class<R> cls, @Nullable final List<String> excludes) {
        final LambdaUpdateWrapper<R> updateWrapper = Wrappers.lambdaUpdate(cls);
        final Map<String, Object> args = from(dto);
        if (!CollectionUtils.isEmpty(args)) {
            buildFieldMap(args, cls, updateWrapper, excludes);
        }
        return updateWrapper;
    }

    public static <T, R> LambdaUpdateWrapper<R> buildUpdateWrapper(@Nonnull final T dto, @Nonnull final Class<R> cls) {
        return buildUpdateWrapper(dto, cls, null);
    }

    public static <T, R> LambdaQueryWrapper<R> buildDeleteWrapper(@Nonnull final T dto, @Nonnull final Class<R> cls, @Nullable final List<String> excludes) {
        final Map<String, Object> args = from(dto);
        return buildQueryWrapper(args, cls, excludes);
    }

    public static <T, R> LambdaQueryWrapper<R> buildDeleteWrapper(@Nonnull final T dto, @Nonnull final Class<R> cls) {
        return buildDeleteWrapper(dto, cls, null);
    }
}
