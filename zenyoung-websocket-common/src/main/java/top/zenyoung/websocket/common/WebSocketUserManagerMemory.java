package top.zenyoung.websocket.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.List;
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
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private final Map<String, Map<String, List<WebSocketSender>>> groupSenders = Maps.newConcurrentMap();

    @Override
    public WebSocketSender put(@Nonnull final String groupKey, @Nonnull final String key, @Nonnull final WebSocketSender sender) {
        log.debug("put(groupKey: {},key: {},sender: {})...", groupKey, key, sender);
        Assert.hasText(groupKey, "'groupKey'不能为空!");
        Assert.hasText(key, "'key'不能为空!");
        //获取分组数据集合
        final String lock = "put:" + key;
        synchronized (LOCKS.computeIfAbsent(lock, k -> new Object())) {
            try {
                final Map<String, List<WebSocketSender>> mapWeakSenders = groupSenders.computeIfAbsent(groupKey, k -> Maps.newConcurrentMap());
                //添加数据
                final List<WebSocketSender> listWeakSenders = mapWeakSenders.computeIfAbsent(key, k -> Lists.newCopyOnWriteArrayList());
                //添加数据
                listWeakSenders.add(sender);
            } finally {
                LOCKS.remove(lock);
            }
        }
        return sender;
    }

    @Override
    public int size(@Nonnull final String groupKey) {
        log.debug("size(groupKey: {})...", groupKey);
        Assert.hasText(groupKey, "'groupKey'不能为空!");
        final Map<String, List<WebSocketSender>> mapSenders = groupSenders.get(groupKey);
        if (!CollectionUtils.isEmpty(mapSenders)) {
            return mapSenders.values().stream()
                    .mapToInt(List::size)
                    .sum();
        }
        return 0;
    }

    @Override
    public List<WebSocketSender> get(@Nonnull final String groupKey, @Nonnull final String key) {
        log.debug("get(groupKey: {},key: {})...", groupKey, key);
        Assert.hasText(groupKey, "'groupKey'不能为空!");
        Assert.hasText(key, "'key'不能为空!");
        final Map<String, List<WebSocketSender>> mapSenders = groupSenders.get(groupKey);
        if (!CollectionUtils.isEmpty(mapSenders)) {
            return mapSenders.get(key);
        }
        return null;
    }

    @Override
    public List<WebSocketSender> remove(@Nonnull final String groupKey, @Nonnull final String key) {
        log.debug("remove(groupKey: {},key: {})...", groupKey, key);
        Assert.hasText(groupKey, "'groupKey'不能为空!");
        Assert.hasText(key, "'key'不能为空!");
        final Map<String, List<WebSocketSender>> mapSenders = groupSenders.get(groupKey);
        if (!CollectionUtils.isEmpty(mapSenders)) {
            return mapSenders.remove(key);
        }
        return null;
    }

    @Override
    public void remove(@Nonnull final String groupKey, @Nonnull final String key, @Nonnull final WebSocketSender sender) {
        log.debug("remove(groupKey: {},key: {},sender: {})...", groupKey, key, sender);
        final Map<String, List<WebSocketSender>> mapSenders = groupSenders.get(groupKey);
        if (!CollectionUtils.isEmpty(mapSenders)) {
            final List<WebSocketSender> weakSenders = mapSenders.get(key);
            if (!CollectionUtils.isEmpty(weakSenders)) {
                weakSenders.stream()
                        .filter(s -> s != null && s.equals(sender))
                        .findFirst()
                        .ifPresent(weakSenders::remove);
            }
        }
    }

    @Override
    public void consumers(@Nonnull final String groupKey, @Nonnull final BiConsumer<String, List<WebSocketSender>> senderConsumer) {
        log.debug("consumers(groupKey: {},senderConsumer: {})...", groupKey, senderConsumer);
        Assert.hasText(groupKey, "'groupKey'不能为空!");
        final Map<String, List<WebSocketSender>> mapSenders = groupSenders.get(groupKey);
        if (!CollectionUtils.isEmpty(mapSenders)) {
            mapSenders.forEach(senderConsumer);
        }
    }
}
