package top.zenyoung.jfx.support;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.env.Environment;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

/**
 * The utility PropertyReaderHelper.
 *
 * @author Felix Roske
 * @author Andreas Jay
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertyReaderHelper {
    /**
     * Lookup in {@link Environment} a certain property or a list of properties.
     *
     * @param env      the {@link Environment} context from which to
     * @param propName the name of the property to lookup from {@link Environment}.
     * @return the list
     */
    public static List<String> get(@Nonnull final Environment env, @Nonnull final String propName) {
        final List<String> list = Lists.newArrayList();
        final String singleProp = env.getProperty(propName);
        if (singleProp != null) {
            list.add(singleProp);
            return list;
        }
        int counter = 0;
        String prop = env.getProperty(propName + "[" + counter + "]");
        while (prop != null) {
            list.add(prop);
            counter++;
            prop = env.getProperty(propName + "[" + counter + "]");
        }
        return list;
    }

    /**
     * Load from {@link Environment} a key with a given type. If sucj key is
     * present supply it in {@link Consumer}.
     *
     * @param <T>      the generic type
     * @param env      the env
     * @param key      the key
     * @param type     the type
     * @param function the function
     */
    public static <T> void setIfPresent(@Nonnull final Environment env, @Nonnull final String key,
                                        @Nonnull final Class<T> type, @Nonnull final Consumer<T> function) {
        final T value = env.getProperty(key, type);
        if (value != null) {
            function.accept(value);
        }
    }

    /**
     * Determine file path from package name creates from class package instance
     * the file path equivalent. The path will be prefixed and suffixed with a
     * slash.
     *
     * @return the path equivalent to a package structure.
     */
    public static String determineFilePathFromPackageName(@Nonnull final Class<?> clazz) {
        return "/" + clazz.getPackage().getName().replace('.', '/') + "/";
    }
}