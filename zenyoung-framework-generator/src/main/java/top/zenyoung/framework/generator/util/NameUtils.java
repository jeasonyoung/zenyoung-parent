package top.zenyoung.framework.generator.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 字段名工具类
 *
 * @author young
 */
public class NameUtils {
    private static final String UNDERLINE = "_";
    /**
     * 是否为大写命名
     */
    private static final Pattern CAPITAL_MODE = Pattern.compile("^[\\dA-Z/_]+$");

    /**
     * 按下划线进行拆分
     *
     * @param param 源字符串
     * @return 拆分后集合
     */
    public static List<String> underlineToSplit(@Nullable final String param) {
        // 快速检查
        if (Strings.isNullOrEmpty(param)) {
            // 没必要转换
            return Lists.newArrayList();
        }
        String tempName = param;
        // 大写数字下划线组成转为小写 , 允许混合模式转为小写
        if (isCapitalMode(param) || isMixedMode(param)) {
            tempName = param.toLowerCase();
        }
        final StringBuilder result = new StringBuilder();
        // 用下划线将原始字符串分割
        return splitter(UNDERLINE, tempName);
    }

    /**
     * 字符串下划线转驼峰格式
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String underlineToCamel(@Nullable final String param) {
        // 用下划线将原始字符串分割
        final List<String> camels = underlineToSplit(param);
        if (CollectionUtils.isEmpty(camels)) {
            return param;
        }
        // 跳过原始字符串中开头、结尾的下换线或双重下划线
        // 处理真正的驼峰片段
        final AtomicInteger refInx = new AtomicInteger(0);
        return camels.stream()
                .filter(camel -> !Strings.isNullOrEmpty(camel))
                .map(camel -> {
                    if (refInx.getAndIncrement() == 0) {
                        // 第一个驼峰片段，首字母都小写
                        return firstToLowerCase(camel);
                    }
                    // 其他的驼峰片段，首字母大写
                    return firstToUpperCase(camel);
                })
                .collect(Collectors.joining());
    }

    /**
     * 是否为大写命名
     *
     * @param word 待判断字符串
     * @return ignore
     */
    public static boolean isCapitalMode(final String word) {
        return !Strings.isNullOrEmpty(word) && CAPITAL_MODE.matcher(word).matches();
    }

    /**
     * 是否为驼峰下划线混合命名
     *
     * @param word 待判断字符串
     * @return ignore
     */
    public static boolean isMixedMode(final String word) {
        return matches(".*[A-Z]+.*", word) && matches(".*[/_]+.*", word);
    }

    /**
     * 正则表达式匹配
     *
     * @param regex 正则表达式字符串
     * @param input 要匹配的字符串
     * @return 如果 input 符合 regex 正则表达式格式, 返回true, 否则返回 false;
     */
    public static boolean matches(final String regex, final String input) {
        if (Strings.isNullOrEmpty(regex) || Strings.isNullOrEmpty(input)) {
            return false;
        }
        return Pattern.matches(regex, input);
    }

    /**
     * 首字母大写
     *
     * @param name 待转换的字符串
     * @return 转换后的字符串
     */
    public static String firstToUpperCase(final String name) {
        if (Strings.isNullOrEmpty(name)) {
            return "";
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * 首字母小写
     *
     * @param name 待转换的字符串
     * @return 转换后的字符串
     */
    public static String firstToLowerCase(final String name) {
        if (Strings.isNullOrEmpty(name)) {
            return "";
        }
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    public static List<String> splitter(final String sep, final String val) {
        if (!Strings.isNullOrEmpty(sep) && !Strings.isNullOrEmpty(val)) {
            return Splitter.on(sep)
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(val);
        }
        return Lists.newArrayList();
    }

    /**
     * 路径拼接
     *
     * @param paths 路径数组
     * @return 拼接结果
     */
    public static String pathJoiner(final String... paths) {
        if (Objects.nonNull(paths)) {
            final String pkgSep = ".", sep = File.separator;
            return Joiner.on(sep)
                    .skipNulls()
                    .join(Stream.of(paths)
                            .map(v -> {
                                if (!Strings.isNullOrEmpty(v)) {
                                    if (v.contains(pkgSep) && v.length() > pkgSep.length()) {
                                        return splitter(pkgSep, v);
                                    } else if (v.contains(sep)) {
                                        if (v.length() == sep.length()) {
                                            return null;
                                        }
                                        return splitter(sep, v);
                                    }
                                    return Lists.newArrayList(v);
                                }
                                return null;
                            })
                            .filter(Objects::nonNull)
                            .flatMap(Collection::stream)
                            .map(val -> {
                                if (Objects.nonNull(val)) {
                                    final int len = val.length();
                                    if (val.startsWith(sep)) {
                                        if (len == sep.length()) {
                                            return null;
                                        }
                                        return val.substring(sep.length());
                                    }
                                    if (val.endsWith(sep)) {
                                        if (len == sep.length()) {
                                            return null;
                                        }
                                        return val.substring(0, sep.length() - 1);
                                    }
                                }
                                return val;
                            })
                            .filter(val -> !Strings.isNullOrEmpty(val))
                            .collect(Collectors.toList())
                    );
        }
        return "";
    }
}
