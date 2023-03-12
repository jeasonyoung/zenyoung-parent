package top.zenyoung.common.util;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 字符串解析工具
 *
 * @author young
 */
@Slf4j
public class StrParseUtils {
    private static final String SEP = ",";

    /**
     * 字符串解析
     *
     * @param strVal            字符串
     * @param sep               分拆字符
     * @param classParseHandler 目标类型解析处理
     * @param <R>               目标类型
     * @return 目标数据集合
     */
    public static <R extends Serializable> List<R> parse(@Nullable final String strVal, @Nonnull final String sep,
                                                         @Nonnull final Function<String, R> classParseHandler) {
        if (!Strings.isNullOrEmpty(strVal)) {
            return Splitter.on(sep).omitEmptyStrings().trimResults().splitToList(strVal)
                    .stream().filter(val -> !Strings.isNullOrEmpty(val))
                    .distinct()
                    .map(val -> {
                        try {
                            return classParseHandler.apply(val);
                        } catch (Throwable ex) {
                            log.warn("parse(val: {})-exp: {}", val, ex.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 字符串解析
     *
     * @param strVal            字符串
     * @param classParseHandler 目标类型解析处理
     * @param <R>               目标类型
     * @return 目标数据集合
     */
    public static <R extends Serializable> List<R> parse(@Nullable final String strVal, @Nonnull final Function<String, R> classParseHandler) {
        return parse(strVal, SEP, classParseHandler);
    }

    /**
     * 解析为长整型集合
     *
     * @param strVal 字符串
     * @return 目标数据集合
     */
    public static List<Long> parseLong(@Nullable final String strVal) {
        return parse(strVal, Long::parseLong);
    }

    /**
     * 解析为整型集合
     *
     * @param strVal 字符串
     * @return 目标数据集合
     */
    public static List<Integer> parseInt(@Nullable final String strVal) {
        return parse(strVal, Integer::parseInt);
    }

    /**
     * 解析为字符串集合
     *
     * @param strVal 字符串
     * @return 目标数据集合
     */
    public static List<String> parse(@Nullable final String strVal) {
        return parse(strVal, val -> val);
    }
}
