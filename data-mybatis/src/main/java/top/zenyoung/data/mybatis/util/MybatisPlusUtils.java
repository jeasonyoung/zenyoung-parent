package top.zenyoung.data.mybatis.util;

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
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MybatisPlus工具类
 *
 * @author young
 */
@Slf4j
@UtilityClass
public class MybatisPlusUtils {
    private static <T> Map<String, Object> fromToMap(@Nonnull final T obj) {
        final Map<String, Object> map = Maps.newHashMap();
        ReflectionUtils.doWithFields(obj.getClass(), f -> {
            f.setAccessible(true);
            final Object val = f.get(obj);
            if (Objects.nonNull(val)) {
                map.put(f.getName(), val);
            }
        });
        return map;
    }

    private static <T> Map<String, Object> from(@Nonnull final T dto) {
        final Map<String, Object> dtoMap = fromToMap(dto);
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

    private static <R> void buildFieldMap(@Nonnull final Map<String, Object> params, @Nonnull final Class<R> cls, @Nullable final List<String> excludes,
                                          @Nonnull final BiConsumer<Pair<String, String>, Object> fieldValHandler) {
        if (!CollectionUtils.isEmpty(params)) {
            final Map<String, ColumnCache> colCacheMap = LambdaUtils.getColumnMap(cls);
            if (!CollectionUtils.isEmpty(colCacheMap)) {
                params.forEach((name, val) -> {
                    final ColumnCache colCache = colCacheMap.get(LambdaUtils.formatKey(name));
                    if (Objects.nonNull(colCache) && Objects.nonNull(val)) {
                        //检查是否排除字段
                        if (!CollectionUtils.isEmpty(excludes) && excludes.contains(name)) {
                            return;
                        }
                        final String colName = colCache.getColumn();
                        fieldValHandler.accept(Pair.of(name, colName), val);
                    }
                });
            }
        }
    }

    public static <R> void buildFieldMap(@Nonnull final Map<String, Object> params, @Nonnull final Class<R> cls,
                                         @Nonnull final LambdaQueryWrapper<R> queryWrapper, @Nullable final List<String> excludes,
                                         @Nonnull final Function<String, SqlKeyword> fieldOpHandler) {
        buildFieldMap(params, cls, excludes, (p, val) -> {
            //查询操作
            final SqlKeyword op = fieldOpHandler.apply(p.getLeft());
            if (Objects.nonNull(op)) {
                //查询处理
                buildQueryFieldMap(queryWrapper, p.getRight(), op, val);
            }
        });
    }

    public static <R> void buildFieldMap(@Nonnull final Map<String, Pair<SqlKeyword, Object>> params, @Nonnull final Class<R> cls,
                                         @Nonnull final LambdaQueryWrapper<R> queryWrapper, @Nullable final List<String> excludes) {
        final Map<String, Object> args = params.entrySet().stream()
                .map(entry -> {
                    final String name = entry.getKey();
                    final Pair<SqlKeyword, Object> v = entry.getValue();
                    return Pair.of(name, v.getRight());
                })
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight, (n, o) -> n));
        buildFieldMap(args, cls, excludes, (p, val) -> {
            final Pair<SqlKeyword, Object> v = params.get(p.getLeft());
            final SqlKeyword op;
            if (Objects.nonNull(v) && Objects.nonNull(op = v.getLeft())) {
                //查询处理
                buildQueryFieldMap(queryWrapper, p.getRight(), op, val);
            }
        });
    }

    public static <R> void buildFieldMap(@Nonnull final Map<String, Object> params, @Nonnull final Class<R> cls,
                                         @Nonnull final LambdaUpdateWrapper<R> updateWrapper, @Nullable final List<String> excludes) {
        //更新处理
        buildFieldMap(params, cls, excludes, (p, val) -> buildUpdateFieldMap(updateWrapper, p.getRight(), val));
    }

    private static <R> String formatSqlHandler(@Nonnull final AbstractLambdaWrapper<R, ?> queryWrapper, @Nonnull final Object val) {
        try {
            final Class<?> cls = queryWrapper.getClass();
            final Method formatSqlMethod = ReflectionUtils.findMethod(cls, "formatSql", String.class, Object[].class);
            if (Objects.nonNull(formatSqlMethod)) {
                formatSqlMethod.setAccessible(true);
                return (String) formatSqlMethod.invoke(queryWrapper, "{0}", new Object[]{val});
            }
        } catch (Exception e) {
            log.error("formatSqlHandler(val: {})-exp: {}", val, e.getMessage());
        }
        return null;
    }

    private static <R> void buildQueryFieldMap(@Nonnull final LambdaQueryWrapper<R> queryWrapper, @Nonnull final String colName, @Nonnull final SqlKeyword op, @Nonnull final Object val) {
        final Class<?> cls = queryWrapper.getClass();
        final Method method = ReflectionUtils.findMethod(cls, "doIt", boolean.class, ISqlSegment[].class);
        if (Objects.nonNull(method)) {
            try {
                final String formatSql = formatSqlHandler(queryWrapper, val);
                if (!Strings.isNullOrEmpty(formatSql)) {
                    final ISqlSegment col = () -> colName;
                    final ISqlSegment colVal = () -> formatSql;
                    method.setAccessible(true);
                    method.invoke(queryWrapper, true, new ISqlSegment[]{col, op, colVal});
                }
            } catch (Exception e) {
                log.warn("buildQueryFieldMap(queryWrapper: {},colCache: {})-exp: {}", queryWrapper, colName, e.getMessage());
            }
        }
    }

    private static <R> void buildUpdateFieldMap(@Nonnull final LambdaUpdateWrapper<R> updateWrapper,
                                                @Nonnull final String colName, @Nonnull final Object val) {
        final String formatSql = formatSqlHandler(updateWrapper, val);
        if (!Strings.isNullOrEmpty(formatSql)) {
            updateWrapper.setSql(true, String.format("%s=%s", colName, formatSql));
        }
    }

    public static <R> LambdaQueryWrapper<R> buildQueryWrapper(@Nonnull final Map<String, Object> params, @Nonnull final Class<R> cls) {
        return buildQueryWrapper(params, cls, (List<String>) null);
    }

    public static <R> LambdaQueryWrapper<R> buildQueryWrapper(@Nonnull final Map<String, Object> params, @Nonnull final Class<R> cls,
                                                              @Nullable final List<String> excludes) {
        return buildQueryWrapper(params, cls, excludes, f -> SqlKeyword.EQ);
    }

    public static <R> LambdaQueryWrapper<R> buildQueryWrapper(@Nonnull final Map<String, Object> params, @Nonnull final Class<R> cls,
                                                              @Nullable final List<String> excludes, @Nonnull final Function<String, SqlKeyword> fieldOpHandler) {
        final LambdaQueryWrapper<R> queryWrapper = Wrappers.lambdaQuery(cls);
        if (!CollectionUtils.isEmpty(params)) {
            buildFieldMap(params, cls, queryWrapper, excludes, fieldOpHandler);
        }
        return queryWrapper;
    }

    public static <R> void buildQueryWrapper(@Nonnull final Map<String, Object> params, @Nonnull final Class<R> cls,
                                             @Nonnull final LambdaQueryWrapper<R> queryWrapper, @Nullable final List<String> excludes) {
        if (!CollectionUtils.isEmpty(params)) {
            buildFieldMap(params, cls, queryWrapper, excludes, f -> SqlKeyword.EQ);
        }
    }

    public static <R> void buildQueryWrapper(@Nonnull final Map<String, Object> params, @Nonnull final Class<R> cls,
                                             @Nonnull final LambdaQueryWrapper<R> queryWrapper) {
        buildQueryWrapper(params, cls, queryWrapper, null);
    }

    public static <T, R> LambdaQueryWrapper<R> buildQueryWrapper(@Nonnull final T dto, @Nonnull final Class<R> cls, @Nullable final List<String> excludes,
                                                                 @Nonnull final Function<String, SqlKeyword> fieldOpHandler) {
        final Map<String, Object> args = from(dto);
        return buildQueryWrapper(args, cls, excludes, fieldOpHandler);
    }

    public static <T, R> LambdaQueryWrapper<R> buildQueryWrapper(@Nonnull final T dto, @Nonnull final Class<R> cls, @Nullable final List<String> excludes) {
        return buildQueryWrapper(dto, cls, excludes, f -> SqlKeyword.EQ);
    }

    public static <T, R> LambdaQueryWrapper<R> buildQueryWrapper(@Nonnull final T dto, @Nonnull final Class<R> cls, @Nonnull final Function<String, SqlKeyword> fieldOpHandler) {
        return buildQueryWrapper(dto, cls, null, fieldOpHandler);
    }

    public static <T, R> LambdaQueryWrapper<R> buildQueryWrapper(@Nonnull final T dto, @Nonnull final Class<R> clazz) {
        return buildQueryWrapper(dto, clazz, (List<String>) null);
    }

    @SuppressWarnings({"unchecked"})
    public static <T> LambdaQueryWrapper<T> buildQueryWrapper(@Nonnull final T dto, @Nonnull final Function<String, SqlKeyword> fieldOpHandler) {
        final Class<T> cls = (Class<T>) dto.getClass();
        return buildQueryWrapper(dto, cls, fieldOpHandler);
    }

    public static <T> LambdaQueryWrapper<T> buildQueryWrapper(@Nonnull final T dto) {
        return buildQueryWrapper(dto, f -> SqlKeyword.EQ);
    }

    public static <T, R> void buildQueryWrapper(@Nonnull final T dto, @Nonnull final Class<R> cls,
                                                @Nonnull final LambdaQueryWrapper<R> queryWrapper, @Nullable final List<String> excludes) {
        final Map<String, Object> args = from(dto);
        if (!CollectionUtils.isEmpty(args)) {
            buildFieldMap(args, cls, queryWrapper, excludes, col -> SqlKeyword.EQ);
        }
    }

    public static <T, R> void buildQueryWrapper(@Nonnull final T dto, @Nonnull final Class<R> cls, @Nonnull final LambdaQueryWrapper<R> queryWrapper) {
        buildQueryWrapper(dto, cls, queryWrapper, null);
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
