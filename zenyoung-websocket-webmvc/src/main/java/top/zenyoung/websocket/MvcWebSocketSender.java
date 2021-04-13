package top.zenyoung.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import top.zenyoung.websocket.common.WebSocketSender;

import javax.annotation.Nonnull;

/**
 * WebSocket-发送器实现
 *
 * @author young
 */
@Slf4j
@AllArgsConstructor(staticName = "of")
public class MvcWebSocketSender implements WebSocketSender {
    private final WebSocketSession session;

    @SneakyThrows
    @Override
    public void sendData(@Nonnull final String data) {
        log.debug("sendData(data: {})...", data);
        this.session.sendMessage(new TextMessage(data));
    }

    @Override
    public <T> void sendJsonData(@Nonnull final ObjectMapper objectMapper, @Nonnull final T data) {
        log.debug("sendJsonData(data: {})...", data);
        try {
            sendData(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException ex) {
            log.debug("sendJsonData(data: {})-exp: {}", data, ex.getMessage());
        }
    }
}
