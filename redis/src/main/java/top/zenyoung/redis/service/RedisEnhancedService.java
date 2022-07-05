package top.zenyoung.redis.service;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Redis增强-服务接口
 * 捕获访问异常
 *
 * @author young
 */
public interface RedisEnhancedService {

    /**
     * Redis调用
     *
     * @param handler 业务处理
     */
    void redisHandler(@Nonnull final Runnable handler);

    /**
     * Redis调用
     *
     * @param data    入参数据
     * @param handler 业务处理
     * @param <T>     入参数据类型
     */
    <T> void redisHandler(@Nonnull final T data, @Nonnull final Consumer<T> handler);

    /**
     * Redis调用
     *
     * @param handler 业务处理
     * @param <T>     返回数据类型
     * @return 返回数据
     */
    <T> T redisHandler(@Nonnull final Supplier<T> handler);
}
