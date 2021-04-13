package top.zenyoung.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;
import top.zenyoung.websocket.common.WebSocketSender;

import javax.annotation.Nonnull;

/**
 * WebSocket-发送器实现
 *
 * @author yangyong
 * @version 1.0
 * date 2020/6/29 4:24 下午
 **/
@Slf4j
@Getter
public class ReactiveWebSocketSender implements WebSocketSender {
    /**
     * WebSocket会话
     */
    private final WebSocketSession session;
    /**
     * WebSocket消息发送器
     */
    private final FluxSink<WebSocketMessage> sink;

    /**
     * 构造函数
     *
     * @param session WebSocket会话
     * @param sink    WebSocket消息发送器
     */
    public ReactiveWebSocketSender(@Nonnull final WebSocketSession session, @Nonnull final FluxSink<WebSocketMessage> sink) {
        this.session = session;
        this.sink = sink;
    }

    /**
     * 发送消息
     *
     * @param data 消息内容
     */
    @Override
    public void sendData(@Nonnull final String data) {
        sink.next(session.textMessage(data));
    }

    /**
     * 发送JSON格式消息
     *
     * @param objectMapper JSON处理器
     * @param data         消息数据
     * @param <T>          消息数据类型
     */
    @Override
    public <T> void sendJsonData(@Nonnull final ObjectMapper objectMapper, @Nonnull final T data) {
        log.debug("sendJsonData(objectMapper: {},data: {})...", objectMapper, data);
        try {
            sendData(objectMapper.writeValueAsString(data));
        } catch (Throwable ex) {
            log.error("sendJsonData(objectMapper: {},data: {})-exp: {}", objectMapper, data, ex.getMessage());
        }
    }
}
