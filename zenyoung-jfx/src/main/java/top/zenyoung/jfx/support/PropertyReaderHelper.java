package top.zenyoung.jfx.support;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.springframework.core.env.Environment;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

/**
 * The utility PropertyReaderHelper.
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/1/23 6:06 下午
 **/
public class PropertyReaderHelper {

    /**
     * Lookup in {@link Environment} a certain property or a list of properties.
     *
     * @param env      the {@link Environment} context from which to
     * @param propName the name of the property to lookup from {@link Environment}.
     * @return the list
     */
    public static List<String> get(@Nonnull final Environment env, @Nonnull final String propName) {
        final String singleProp = env.getProperty(propName);
        if (!Strings.isNullOrEmpty(singleProp)) {
            return Lists.newArrayList(singleProp);
        }
        final List<String> list = Lists.newLinkedList();
        int counter = 0;
        String propVal = env.getProperty(propName + "[" + counter + "]");
        while (propVal != null) {
            if (!Strings.isNullOrEmpty(propVal)) {
                list.add(propVal);
            }
            counter++;
            propVal = env.getProperty(propName + "[" + counter + "]");
        }
        return list;
    }

    /**
     * Load from {@link Environment} a key with a given type. If sucj key is
     * present supply it in {@link Consumer}.
     *
     * @param env      the env
     * @param key      the key
     * @param type     the type
     * @param consumer the function
     * @param <T>      the generic type
     */
    public static <T> void setIfPresent(@Nonnull final Environment env, @Nonnull final String key, @Nonnull final Class<T> type, @Nonnull final Consumer<T> consumer) {
        final T val = env.getProperty(key, type);
        if (val != null) {
            consumer.accept(val);
        }
    }

    /**
     * Determine file path from package name creates from class package instance
     * the file path equivalent. The path will be prefixed and suffixed with a slash.
     *
     * @param clazz the type
     * @return the path equivalent to a package structure.
     */
    public static String determineFilePathFromPackageName(@Nonnull final Class<?> clazz) {
        return "/" + clazz.getPackage().getName().replace('.', '/') + "/";
    }
}
