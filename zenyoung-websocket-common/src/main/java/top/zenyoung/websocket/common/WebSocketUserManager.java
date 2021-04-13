package top.zenyoung.websocket.common;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * WebSocket用户管理器接口
 *
 * @author yangyong
 * @version 1.0
 * date 2020/6/30 2:03 下午
 **/
public interface WebSocketUserManager {

    /**
     * 添加分组WebSocket用户
     *
     * @param groupKey 分组键
     * @param key      用户键
     * @param sender   WebSocket用户
     * @return WebSocket用户
     */
    WebSocketSender put(@Nonnull final String groupKey, @Nonnull final String key, @Nonnull final WebSocketSender sender);

    /**
     * 获取分组数量
     *
     * @param groupKey 分组键
     * @return 数量
     */
    int size(@Nonnull final String groupKey);

    /**
     * 获取分组WebSocket用户
     *
     * @param groupKey 分组键
     * @param key      用户键
     * @return WebSocket用户
     */
    List<WebSocketSender> get(@Nonnull final String groupKey, @Nonnull final String key);

    /**
     * 移除分组WebSocket用户
     *
     * @param groupKey 分组键
     * @param key      用户键
     * @return WebSocket用户
     */
    List<WebSocketSender> remove(@Nonnull final String groupKey, @Nonnull final String key);

    /**
     * 移除分组指定的WebSocket用户
     *
     * @param groupKey 分组键
     * @param key      用户键
     * @param sender   指定的WebSocket
     */
    void remove(@Nonnull final String groupKey, @Nonnull final String key, @Nonnull final WebSocketSender sender);

    /**
     * 分组群发消费处理
     *
     * @param groupKey       分组键
     * @param senderConsumer WebSocket用户消费处理
     */
    void consumers(@Nonnull final String groupKey, @Nonnull final BiConsumer<String, List<WebSocketSender>> senderConsumer);
}