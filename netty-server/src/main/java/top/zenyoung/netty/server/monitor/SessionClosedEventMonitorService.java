package top.zenyoung.netty.server.monitor;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.zenyoung.netty.event.ClosedEvent;
import top.zenyoung.netty.server.session.ChannelSessionMap;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

/**
 * 会话关闭时间监听处理-服务
 *
 * @author young
 */
@Slf4j
@Component
public class SessionClosedEventMonitorService {
    private final static Map<String, Object> LOCKS = Maps.newConcurrentMap();

    @Async
    @EventListener({ClosedEvent.class})
    public void onClosedEvent(@Nonnull final ClosedEvent event) {
        Optional.ofNullable(event.getDeviceId())
                .filter(deviceId -> !Strings.isNullOrEmpty(deviceId))
                .ifPresent(deviceId -> {
                    synchronized (LOCKS.computeIfAbsent(deviceId, k -> new Object())) {
                        try {
                            log.info("onClosedEvent=> {}", event);
                            ChannelSessionMap.getById(deviceId)
                                    .ifPresent(ChannelSessionMap::remove);
                        } finally {
                            LOCKS.remove(deviceId);
                        }
                    }
                });

    }
}
