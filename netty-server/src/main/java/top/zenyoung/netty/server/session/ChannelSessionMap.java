package top.zenyoung.netty.server.session;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.netty.session.Session;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Socket通信会话Map
 *
 * @author young
 */
@Slf4j
public class ChannelSessionMap {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private static final Map<String, Session> SESSIONS = Maps.newConcurrentMap();

    /**
     * 获取当前会话总数
     *
     * @return 会话总数
     */
    public static int getTotals() {
        return SESSIONS.size();
    }

    /**
     * 添加到map
     *
     * @param session 会话
     */
    public static void put(@Nonnull final Session session) {
        Optional.ofNullable(session.getDeviceId())
                .filter(deviceId -> !Strings.isNullOrEmpty(deviceId))
                .ifPresent(deviceId -> {
                    synchronized (LOCKS.computeIfAbsent(deviceId, k -> new Object())) {
                        try {
                            SESSIONS.put(deviceId, session);
                        } finally {
                            LOCKS.remove(deviceId);
                            log.info("当前Session数量: {}", getTotals());
                        }
                    }
                });
    }

    /**
     * 根据设备ID获取会话
     *
     * @param deviceId 设备ID
     * @return 会话
     */
    public static Session getByDevice(@Nonnull final String deviceId) {
        return Optional.of(deviceId)
                .filter(id -> !Strings.isNullOrEmpty(id))
                .map(id -> SESSIONS.getOrDefault(id, null))
                .orElse(null);
    }

    /**
     * 根据设备ID获取会话
     *
     * @param deviceId 设备ID
     * @return 会话
     */
    public static Optional<Session> getById(@Nonnull final String deviceId) {
        return Optional.ofNullable(getByDevice(deviceId));
    }

    /**
     * 从Map移除
     *
     * @param session 会话
     */
    public static void remove(@Nonnull final Session session) {
        Optional.ofNullable(session.getDeviceId())
                .filter(deviceId -> !Strings.isNullOrEmpty(deviceId) && SESSIONS.containsKey(deviceId))
                .ifPresent(deviceId -> {
                    synchronized (LOCKS.computeIfAbsent(deviceId, k -> new Object())) {
                        try {
                            final Session s = SESSIONS.remove(deviceId);
                            if (Objects.nonNull(s) && s.getStatus()) {
                                s.close();
                            }
                            log.info("移除Session:" + session);
                        } catch (Throwable e) {
                            log.warn("remove(deviceId: {})-exp: {}", deviceId, e.getMessage());
                        } finally {
                            LOCKS.remove(deviceId);
                            log.info("当前Session数量: {}", getTotals());
                        }
                    }
                });
    }
}
