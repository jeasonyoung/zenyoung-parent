package top.zenyoung.generator.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 路径工具类
 *
 * @author young
 */
public class PathUtils {

    public static String removePrefix(@Nullable final String path, @Nullable final String prefix) {
        if (!Strings.isNullOrEmpty(path) && !Strings.isNullOrEmpty(prefix) && path.startsWith(prefix)) {
            return path.substring(prefix.length());
        }
        return path;
    }

    public static String commonPrefix(@Nullable final List<String> paths) {
        if (!CollectionUtils.isEmpty(paths)) {
            final Map<char[], Integer> pathCharMap = paths.stream()
                    .filter(path -> !Strings.isNullOrEmpty(path))
                    .collect(Collectors.toMap(String::toCharArray, String::length, (o, n) -> n));
            final int maxPrefixLen = pathCharMap.values().stream().mapToInt(v -> v).min().orElse(0);
            if (maxPrefixLen > 0) {
                final List<char[]> chars = Lists.newArrayList(pathCharMap.keySet());
                int p = 0;
                while (p < maxPrefixLen) {
                    final int idx = p;
                    final List<Character> characters = chars.stream()
                            .map(chs -> chs[idx])
                            .distinct()
                            .collect(Collectors.toList());
                    if (characters.size() == 1) {
                        p++;
                        continue;
                    }
                    break;
                }
                if (p > 0) {
                    final int idx = p - 1;
                    final List<Boolean> rets = paths.stream()
                            .filter(path -> !Strings.isNullOrEmpty(path))
                            .map(path -> validSurrogatePairAt(path, idx))
                            .filter(r -> r)
                            .collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(rets)) {
                        p = idx;
                    }
                    final char[] first = pathCharMap.entrySet().stream()
                            .filter(entry -> entry.getValue() > 0)
                            .map(Map.Entry::getKey)
                            .findFirst().orElse(null);
                    if (Objects.nonNull(first)) {
                        return new String(first, 0, p);
                    }
                }
            }
        }
        return null;
    }

    private static boolean validSurrogatePairAt(final CharSequence string, final int index) {
        return index >= 0
                && index <= (string.length() - 2)
                && Character.isHighSurrogate(string.charAt(index))
                && Character.isLowSurrogate(string.charAt(index + 1));
    }
}
