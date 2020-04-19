package top.zenyoung.service;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.time.Duration;

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
    <T extends Serializable> void addCache(@Nonnull final String key, @Nonnull final T data);

    /**
     * 添加缓存数据
     *
     * @param key      缓存键
     * @param data     缓存数据
     * @param liveTime 缓存生存期
     * @param <T>      缓存数据类型
     */
    <T extends Serializable> void addCache(@Nonnull final String key, @Nonnull final T data, @Nonnull final Duration liveTime);

    /**
     * 获取缓存数据
     *
     * @param key       缓存键
     * @param dataClass 缓存数据类型Class
     * @param <T>       缓存数据类型
     * @return 缓存数据
     */
    <T extends Serializable> T getCache(@Nonnull final String key, @Nonnull final Class<T> dataClass);
}
