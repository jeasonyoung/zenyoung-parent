package top.zenyoung.generator.service;

import top.zenyoung.generator.model.DatabaseConnect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 代码生成-缓存-服务接口
 *
 * @author young
 */
public interface GeneratorCacheService {

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

    /**
     * 创建访问令牌
     *
     * @return 访问令牌
     */
    String createToken();

    /**
     * 验证访问令牌
     *
     * @param token 访问令牌
     * @return 令牌是否合法
     */
    boolean verifyToken(@Nullable final String token);
}
