package top.zenyoung.service;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * 缓存-服务接口
 *
 * @author yangyong
 * @version 1.0
 **/
public interface CacheService {

    /**
     * 添加缓存数据
     *
     * @param key  缓存键
     * @param data 缓存数据
     * @param <T>  缓存数据类型
     */
    <T> void addCache(@Nonnull final String key, @Nonnull final T data);

    /**
     * 添加缓存数据
     *
     * @param key      缓存键
     * @param data     缓存数据
     * @param liveTime 缓存生存期
     * @param <T>      缓存数据类型
     */
    <T> void addCache(@Nonnull final String key, @Nonnull final T data, @Nonnull final Duration liveTime);

    /**
     * 获取缓存数据
     *
     * @param key       缓存键
     * @param dataClass 缓存数据类型Class
     * @param <T>       缓存数据类型
     * @return 缓存数据
     */
    <T> T getCache(@Nonnull final String key, @Nonnull final Class<T> dataClass);

    /**
     * 清除缓存
     *
     * @param key 缓存键
     */
    void clear(@Nonnull final String key);

    /**
     * 缓存续约时间
     *
     * @param key         缓存键
     * @param renewalTime 续约时间
     */
    void renewal(@Nonnull final String key, @Nonnull final Duration renewalTime);

    /**
     * 数据缓存处理
     *
     * @param key         缓存键
     * @param dataClass   缓存数据Class
     * @param expire      缓存有效期
     * @param dataHandler 数据处理
     * @param <T>         数据类型
     * @return 数据
     */
    <T> T cacheHander(@Nonnull final String key, @Nonnull final Class<T> dataClass,
                      @Nonnull final Duration expire, @Nonnull final Supplier<T> dataHandler);
}
