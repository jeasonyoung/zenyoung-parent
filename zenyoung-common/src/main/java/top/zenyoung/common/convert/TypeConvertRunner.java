package top.zenyoung.common.convert;

import top.zenyoung.common.selector.ItemBuilder;
import top.zenyoung.common.selector.Selector;

/**
 * 类型转换执行器
 *
 * @author young
 */
public class TypeConvertRunner {
    /**
     * 使用指定参数构建一个选择器
     *
     * @param param 参数
     * @return 返回选择器
     */
    public static <T> Selector<String, T> use(final String param) {
        return new Selector<>(param.toLowerCase());
    }

    /**
     * 这个事项构建器用于构建用于支持 {@link String#contains(CharSequence)} 的事项
     *
     * @param value 字符串数据
     * @return 事项构建者
     * @see #containsAny(CharSequence...)
     */
    public static <T> ItemBuilder<String, T> contains(final CharSequence value) {
        return ItemBuilder.of(s -> s.contains(value));
    }

    /**
     * 包含任意字符串
     *
     * @see #contains(CharSequence)
     */
    public static <T> ItemBuilder<String, T> containsAny(final CharSequence... values) {
        return ItemBuilder.of(s -> {
            for (CharSequence value : values) {
                if (s.contains(value)) {
                    return true;
                }
            }
            return false;
        });
    }
}
