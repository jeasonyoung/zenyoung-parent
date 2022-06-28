package top.zenyoung.common.selector;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 事项构建者
 *
 * @author young
 */
public interface ItemBuilder<P, T> {
    /**
     * 使用一个值工厂构造出一个事项
     *
     * @param factory 值工厂
     * @return 返回事项
     */
    Item<P, T> then(final Function<P, T> factory);

    /**
     * 从值构建出一个事项
     *
     * @param value 值
     * @return 返回一个事项
     */
    default Item<P, T> then(final T value) {
        return then(p -> value);
    }

    /**
     * 工厂函数，用于创建事项构建者
     *
     * @param item 事项
     * @param <P>  参数类型
     * @param <T>  返回值类型
     * @return 返回一个事项创建者
     */
    static <P, T> ItemBuilder<P, T> of(final Predicate<P> item) {
        return factory -> Item.of(item, factory);
    }
}
