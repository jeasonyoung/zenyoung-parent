package top.zenyoung.generator.service;

import top.zenyoung.generator.model.DatabaseConnect;

import javax.annotation.Nonnull;

/**
 * 数据库链接缓存-服务接口
 *
 * @author young
 */
public interface GeneratorConnectCacheService {

    /**
     * 数据库链接存储-添加
     *
     * @param key    存储键
     * @param config 链接数据
     */
    void putConnect(@Nonnull final String key, @Nonnull final DatabaseConnect config);

    /**
     * 数据库链接存储-加载
     *
     * @param key 存储键
     * @return 链接数据
     */
    DatabaseConnect getConnect(@Nonnull final String key);
}
