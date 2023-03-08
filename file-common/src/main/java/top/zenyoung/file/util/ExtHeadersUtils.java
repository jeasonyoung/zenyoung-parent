package top.zenyoung.file.util;

import com.google.common.base.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * 后缀Headers处理工具
 *
 * @author young
 */
public class ExtHeadersUtils {
    private final static String EXT_PREFIX = ".";

    public static void handler(@Nullable final Map<String, Map<String, String>> extHeaders,
                               @Nullable final String ext, @Nonnull final BiConsumer<String, String> headerHandler) {
        if (Objects.nonNull(extHeaders) && !Strings.isNullOrEmpty(ext)) {
            final String key = ext.startsWith(EXT_PREFIX) ? ext : EXT_PREFIX + ext;
            final Map<String, String> headers = extHeaders.getOrDefault(key, null);
            if (Objects.nonNull(headers)) {
                headers.entrySet().stream()
                        .filter(entry -> !Strings.isNullOrEmpty(entry.getKey()) && !Strings.isNullOrEmpty(entry.getValue()))
                        .forEach(entry -> headerHandler.accept(entry.getKey(), entry.getValue()));
            }
        }
    }
}
