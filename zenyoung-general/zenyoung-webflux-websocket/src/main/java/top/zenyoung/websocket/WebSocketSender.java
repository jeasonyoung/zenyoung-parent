package top.zenyoung.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * WebSocket-用户发送器
 *
 * @author yangyong
 * @version 1.0
 * date 2020/6/29 4:24 下午
 **/
@Slf4j
@Getter
public class WebSocketSender implements Serializable {
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
    public WebSocketSender(@Nonnull final WebSocketSession session, @Nonnull final FluxSink<WebSocketMessage> sink) {
        this.session = session;
        this.sink = sink;
    }

    /**
     * 发送消息
     *
     * @param data 消息内容
     */
    public void sendData(@Nonnull final String data) {
        log.info("sendData(data: {})...", data);
        sink.next(session.textMessage(data));
    }

    /**
     * 发送JSON格式消息
     *
     * @param objectMapper JSON处理器
     * @param data         消息数据
     * @param <T>          消息数据类型
     */
    public <T> void sendJsonData(@Nonnull final ObjectMapper objectMapper, @Nonnull final T data) {
        log.debug("sendJsonData(objectMapper: {},data: {})...", objectMapper, data);
        try {
            sendData(objectMapper.writeValueAsString(data));
        } catch (Throwable ex) {
            log.error("sendJsonData(objectMapper: {},data: {})-exp: {}", objectMapper, data, ex.getMessage());
        }
    }
}