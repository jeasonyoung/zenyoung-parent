package top.zenyoung.common.util;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 反射类型工具
 *
 * @author young
 */
public class ClassUtils {

    /**
     * 字段处理器
     *
     * @param cls      类型
     * @param consumer 字段处理器
     */
    public static void getFieldHandlers(@Nonnull final Class<?> cls, @Nonnull final Consumer<Field> consumer) {
        if (cls != Object.class) {
            Stream.of(cls.getDeclaredFields())
                    .filter(Objects::nonNull)
                    .forEach(consumer);
            //递归处理父类
            final Class<?> parent = cls.getSuperclass();
            if (parent != null && parent != Object.class) {
                getFieldHandlers(parent, consumer);
            }
        }
    }

    /**
     * 从类型中反射查找字段
     *
     * @param cls       类型
     * @param predicate 检查查找字段
     * @return 结果字段
     */
    public static Field findFieldByClass(@Nonnull final Class<?> cls, @Nonnull final Predicate<Field> predicate) {
        if (cls != Object.class) {
            //获取全部字段
            final Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                if (predicate.test(f)) {
                    return f;
                }
            }
            //获取父类
            final Class<?> parent = cls.getSuperclass();
            if (parent != null) {
                return findFieldByClass(parent, predicate);
            }
        }
        return null;
    }

    /**
     * 递归获取类型所有字段
     *
     * @param cls        类型
     * @param listFields 字段集合
     */
    public static void getAllFieldsWithSuper(@Nonnull final Class<?> cls, @Nonnull final List<Field> listFields) {
        findFieldByClass(cls, f -> {
            if (listFields.contains(f)) {
                listFields.add(f);
            }
            return false;
        });
    }
}
