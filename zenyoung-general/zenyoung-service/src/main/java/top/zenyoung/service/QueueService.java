package top.zenyoung.service;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.function.Consumer;

/**
 * 队列-服务接口
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/4/18 2:41 下午
 **/
public interface QueueService {

    /**
     * 数据入队
     *
     * @param key  队列键名
     * @param data 队列数据
     * @param <T>  队列数据类型
     */
    <T extends Serializable> void pushQueue(@Nonnull final String key, @Nonnull final T data);

    /**
     * 数据出队
     *
     * @param key       队列键名
     * @param dataClass 队列数据类型Class
     * @param consumer  出队数据处理
     * @param <T>       队列数据类型
     * @return 出队数据处理数量
     */
    <T extends Serializable> int popQueue(@Nonnull final String key, @Nonnull final Class<T> dataClass, @Nonnull final Consumer<T> consumer);
}
