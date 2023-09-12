package top.zenyoung.file.util;

import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;

/**
 * 字符串整理工具
 *
 * @author young
 */
@UtilityClass
public class TrimUtils {
    /**
     * 径路分隔符
     */
    public static final String PATH_SEP = "/";

    public static String trimPathPrefix(@Nonnull final String path) {
        return trimPrefix(path, PATH_SEP);
    }

    public static String trimPathSuffix(@Nonnull final String path) {
        return trimSuffix(path, PATH_SEP);
    }

    public static String trimPrefix(@Nonnull final String data, @Nonnull final String prefix) {
        if (!Strings.isNullOrEmpty(data) && !Strings.isNullOrEmpty(prefix) && data.startsWith(prefix)) {
            return trimPrefix(data.substring(prefix.length()), prefix);
        }
        return data;
    }

    public static String trimSuffix(@Nonnull final String data, @Nonnull final String suffix) {
        if (!Strings.isNullOrEmpty(data) && !Strings.isNullOrEmpty(suffix) && data.endsWith(suffix)) {
            return trimSuffix(data.substring(0, data.length() - suffix.length()), suffix);
        }
        return data;
    }
}
