package top.zenyoung.orm.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import top.zenyoung.common.util.MapUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * MybatisPlus工具类
 *
 * @author young
 */
@Slf4j
public class MybatisPlusUtils {

    public static <R> SFunction<R, ?> buildFunction(@Nonnull final Field field) {
        field.setAccessible(true);
        final AtomicBoolean has = new AtomicBoolean(true);
        final SFunction<R, ?> sf = d -> {
            try {
                return field.get(d);
            } catch (Throwable e) {
                has.set(false);
                log.warn("buildQueryWrapper-[name:{}]-exp: {}", field.getName(), e.getMessage());
            }
            return null;
        };
        return has.get() ? sf : null;
    }

    public static <R> LambdaQueryWrapper<R> buildQueryWrapper(@Nonnull final Map<String, Object> params, @Nonnull final Class<R> clazz,
                                                              @Nonnull final BiConsumer<String, Triple<LambdaQueryWrapper<R>, SFunction<R, ?>, Object>> fieldQueryHandler) {
        final LambdaQueryWrapper<R> queryWrapper = Wrappers.lambdaQuery(clazz);
        if (!CollectionUtils.isEmpty(params)) {
            final Map<String, SFunction<R, ?>> fieldMap = Maps.newHashMap();
            ReflectionUtils.doWithFields(clazz, f -> {
                final String name = f.getName();
                if (!Strings.isNullOrEmpty(name) && params.containsKey(name)) {
                    final Object v = params.get(name);
                    if (Objects.nonNull(v)) {
                        final SFunction<R, ?> sf = buildFunction(f);
                        if (Objects.nonNull(sf)) {
                            fieldMap.put(name, sf);
                        }
                    }
                }
            });
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
        final Map<String, Object> args = MapUtils.from(dto);
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
}