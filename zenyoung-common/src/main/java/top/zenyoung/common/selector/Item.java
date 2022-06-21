package top.zenyoung.common.selector;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 事项
 *
 * @param <P>
 * @param <T>
 * @author young
 */
public interface Item<P, T> {
    /**
     * 执行
     *
     * @return 执行条件
     */
    Predicate<P> execute();

    /**
     * 值工厂
     *
     * @return 工厂处理
     */
    Function<P, T> factory();

    /**
     * 工厂方法，快速创建事项
     *
     * @param runner  测试器
     * @param factory 值工厂
     * @param <P>     参数类型
     * @param <T>     值类型
     * @return 返回一个新的事项
     */
    static <P, T> Item<P, T> of(final Predicate<P> runner, final Function<P, T> factory) {
        return new Item<P, T>() {

            @Override
            public Predicate<P> execute() {
                return runner;
            }

            @Override
            public Function<P, T> factory() {
                return factory;
            }
        };
    }
}
