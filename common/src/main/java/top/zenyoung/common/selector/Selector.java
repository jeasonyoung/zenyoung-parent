package top.zenyoung.common.selector;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 选择器
 *
 * @author young
 */
@RequiredArgsConstructor
public class Selector<P, T> {
    private final P param;
    @Getter
    private boolean selected = false;
    private Function<P, T> factory;

    /**
     * 使用指定的参数创建选择器
     *
     * @param param 参数
     * @param <P>   参数类型
     * @param <T>   返回值类型
     * @return 返回新的选择器
     */
    public static <P, T> Selector<P, T> param(final P param) {
        return new Selector<>(param);
    }

    /**
     * 传入一个新的事项，如果这个事项满足条件
     *
     * @param item 则当前选择器将接受当前事项的结果并执行
     * @return 选择器自身
     */
    public Selector<P, T> test(final Item<P, T> item) {
        if (!this.isSelected()) {
            final boolean pass = item.execute().test(this.param);
            if (pass) {
                this.selected = true;
                this.factory = item.factory();
            }
        }
        return this;
    }

    /**
     * 获取结果，如果当前选择器没有击中任何条件事项，则从给定的提供者中获取结果；
     * 否则将使用当前选择器选中的事项
     *
     * @param supplier 默认值提供者
     * @return 如果有事项被击中，则返回事项值，否则返回参数提供的值
     */
    public T or(final Supplier<T> supplier) {
        return this.selected ? this.factory.apply(param) : supplier.get();
    }

    /**
     * 获取结果
     *
     * @param t 给定默认值
     * @return 如果有事项被击中，则返回事项值，否则返回参数
     * @see #or(Supplier)
     */
    public T or(final T t) {
        return or(() -> t);
    }

    @Override
    public String toString() {
        return String.format("Selector{success=%s}", selected);
    }
}
