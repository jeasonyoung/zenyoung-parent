package top.zenyoung.retrofit;

import javax.annotation.Nonnull;

/**
 * 降级工厂接口
 *
 * @param <T> 接口类型
 */
public interface FallbackFactory<T> {
    /**
     * 创建接口类型对象
     *
     * @param cause 异常
     * @return 接口对象
     */
    @Nonnull
    T create(@Nonnull final Throwable cause);
}
