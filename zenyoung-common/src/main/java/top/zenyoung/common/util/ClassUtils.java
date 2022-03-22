package top.zenyoung.common.util;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;

/**
 * 反射类型工具
 *
 * @author young
 */
public class ClassUtils {

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
            if (fields.length > 0) {
                for (Field f : fields) {
                    if (predicate.test(f)) {
                        return f;
                    }
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
