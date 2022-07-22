package top.zenyoung.orm.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import top.zenyoung.common.util.MapUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * MybatisPlus工具类
 *
 * @author young
 */
@Slf4j
public class MybatisPlusUtils {

    private static <T> Map<String, Object> from(@Nonnull final T dto) {
        return MapUtils.from(dto).entrySet().stream()
                .map(entry -> {
                    final String key = entry.getKey();
                    final Object val = entry.getValue();
                    if (!Strings.isNullOrEmpty(key) && Objects.nonNull(val)) {
                        if (val instanceof String) {
                            if (Strings.isNullOrEmpty((String) val)) {
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

    private static <R> Map<String, SFunction<R, Object>> buildFieldMap(@Nonnull final Map<String, Object> params, @Nonnull final Class<R> cls) {
        final Map<String, SFunction<R, Object>> fieldMap = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(params)) {
            final String prefix = "get";
            final Map<String, Method> methodMap = Maps.newHashMap();
            ReflectionUtils.doWithMethods(cls, method -> {
                final String methodName = method.getName();
                if (methodName.startsWith(prefix)) {
                    methodMap.put(methodName.toLowerCase(), method);
                }
            });
            if (!CollectionUtils.isEmpty(methodMap)) {
                params.forEach((name, val) -> {
                    final String m = (prefix + name).toLowerCase();
                    final Method method = methodMap.getOrDefault(m, null);
                    if (Objects.nonNull(val) && Objects.nonNull(method)) {
                        final AtomicBoolean ref = new AtomicBoolean(true);
                        final SFunction<R, Object> fn = d -> {
                            try {
                                return method.invoke(d);
                            } catch (Throwable e) {
                                ref.set(false);
                                log.warn("buildFieldMap-SFunction: {}-exp: {}", name, e.getMessage());
                            }
                            return null;
                        };
                        if (ref.get()) {
                            fieldMap.put(name, fn);
                        }
                    }
                });
            }
        }
        return fieldMap;
    }

    public static <R> LambdaQueryWrapper<R> buildQueryWrapper(@Nonnull final Map<String, Object> params, @Nonnull final Class<R> cls,
                                                              @Nonnull final BiConsumer<String, Triple<LambdaQueryWrapper<R>, SFunction<R, ?>, Object>> fieldQueryHandler) {
        final LambdaQueryWrapper<R> queryWrapper = Wrappers.lambdaQuery(cls);
        if (!CollectionUtils.isEmpty(params)) {
            final Map<String, SFunction<R, Object>> fieldMap = buildFieldMap(params, cls);
            if (!CollectionUtils.isEmpty(fieldMap)) {
                fieldMap.forEach((col, fn) -> {
                    final Object val = params.get(col);
                    if (Objects.nonNull(val) && Objects.nonNull(fn)) {
                        fieldQueryHandler.accept(col, Triple.of(queryWrapper, fn, val));
                    }
                });
            }
        }
        return queryWrapper;
    }

    public static <T, R> LambdaQueryWrapper<R> buildQueryWrapper(@Nonnull final T dto, @Nonnull final Class<R> clazz,
                                                                 @Nonnull final BiConsumer<String, Triple<LambdaQueryWrapper<R>, SFunction<R, ?>, Object>> fieldQueryHandler) {
        final Map<String, Object> args = from(dto);
        return buildQueryWrapper(args, clazz, fieldQueryHandler);
    }

    public static <T, R> LambdaQueryWrapper<R> buildQueryWrapper(@Nonnull final T dto, @Nonnull final Class<R> clazz) {
        return buildQueryWrapper(dto, clazz, (fieldName, triple) -> defEqHandler(triple));
    }

    public static <T, R> LambdaQueryWrapper<R> buildQueryWrapper(@Nonnull final Map<String, Object> params, @Nonnull final Class<R> clazz) {
        return buildQueryWrapper(params, clazz, (fieldName, triple) -> defEqHandler(triple));
    }

    public static <T, R> void defEqHandler(@Nonnull final Triple<LambdaQueryWrapper<R>, SFunction<R, ?>, Object> triple) {
        final LambdaQueryWrapper<R> queryWrapper = triple.getLeft();
        final SFunction<R, ?> column = triple.getMiddle();
        final Object val = triple.getRight();
        queryWrapper.eq(column, val);
    }

    public static <T, R> LambdaUpdateWrapper<R> buildUpdateWrapper(@Nonnull final T dto, @Nonnull final Class<R> cls) {
        return buildUpdateWrapper(dto, cls, null);
    }

    public static <T, R> LambdaUpdateWrapper<R> buildUpdateWrapper(@Nonnull final T dto, @Nonnull final Class<R> cls, @Nullable final List<String> excludes) {
        final LambdaUpdateWrapper<R> updateWrapper = Wrappers.lambdaUpdate(cls);
        final Map<String, Object> args = from(dto);
        if (!CollectionUtils.isEmpty(args)) {
            final Map<String, SFunction<R, Object>> fieldMap = buildFieldMap(args, cls);
            if (!CollectionUtils.isEmpty(fieldMap)) {
                fieldMap.forEach((col, fn) -> {
                    if (!CollectionUtils.isEmpty(excludes) && excludes.contains(col)) {
                        return;
                    }
                    final Object val = args.get(col);
                    if (Objects.nonNull(val) && Objects.nonNull(fn)) {
                        updateWrapper.set(fn, val);
                    }
                });
            }
        }
        return updateWrapper;
    }

}
