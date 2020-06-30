package top.zenyoung.websocket;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * WebSocket用户管理器默认实现
 *
 * @author yangyong
 * @version 1.0
 * date 2020/6/29 4:22 下午
 **/
@Slf4j
public class WebSocketUserManagerMemory implements WebSocketUserManager {
    private final Map<String, Map<String, WebSocketSender>> groupSenders = Maps.newConcurrentMap();

    @Override
    public WebSocketSender put(@Nonnull final String groupKey, @Nonnull final String key, @Nonnull final WebSocketSender sender) {
        log.debug("put(groupKey: {},key: {},sender: {})...", groupKey, key, sender);
        Assert.hasText(groupKey, "'groupKey'不能为空!");
        Assert.hasText(key, "'key'不能为空!");
        //获取分组数据集合
        final Map<String, WebSocketSender> senders = groupSenders.computeIfAbsent(groupKey, k -> Maps.newConcurrentMap());
        //添加数据
        return senders.put(key, sender);
    }

    @Override
    public int size(@Nonnull final String groupKey) {
        log.debug("size(groupKey: {})...", groupKey);
        Assert.hasText(groupKey, "'groupKey'不能为空!");
        final Map<String, WebSocketSender> senders = groupSenders.get(groupKey);
        return CollectionUtils.isEmpty(senders) ? 0 : senders.size();
    }

    @Override
    public WebSocketSender get(@Nonnull final String groupKey, @Nonnull final String key) {
        log.debug("get(groupKey: {},key: {})...", groupKey, key);
        Assert.hasText(groupKey, "'groupKey'不能为空!");
        Assert.hasText(key, "'key'不能为空!");
        final Map<String, WebSocketSender> senders = groupSenders.get(groupKey);
        if (!CollectionUtils.isEmpty(senders)) {
            return senders.get(key);
        }
        return null;
    }

    @Override
    public WebSocketSender remove(@Nonnull final String groupKey, @Nonnull final String key) {
        log.debug("remove(groupKey: {},key: {})...", groupKey, key);
        Assert.hasText(groupKey, "'groupKey'不能为空!");
        Assert.hasText(key, "'key'不能为空!");
        final Map<String, WebSocketSender> senders = groupSenders.get(groupKey);
        if (!CollectionUtils.isEmpty(senders)) {
            return senders.remove(key);
        }
        return null;
    }

    @Override
    public void consumers(@Nonnull final String groupKey, @Nonnull final BiConsumer<String, WebSocketSender> senderConsumer) {
        log.debug("consumers(groupKey: {},senderConsumer: {})...", groupKey, senderConsumer);
        Assert.hasText(groupKey, "'groupKey'不能为空!");
        final Map<String, WebSocketSender> senders = groupSenders.get(groupKey);
        if (!CollectionUtils.isEmpty(senders)) {
            senders.forEach(senderConsumer);
        }
    }
}
