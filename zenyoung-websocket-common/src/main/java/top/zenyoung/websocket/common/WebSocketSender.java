package top.zenyoung.websocket.common;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * WebSocket-用户发送器接口
 *
 * @author yangyong
 * @version 1.0
 * date 2020/6/29 4:24 下午
 **/
public interface WebSocketSender extends Serializable {
    /**
     * 发送消息
     *
     * @param data 消息内容
     */
    void sendData(@Nonnull final String data);

    /**
     * 发送JSON格式消息
     *
     * @param objectMapper JSON处理器
     * @param data         消息数据
     * @param <T>          消息数据类型
     */
    <T> void sendJsonData(@Nonnull final ObjectMapper objectMapper, @Nonnull final T data);
}
