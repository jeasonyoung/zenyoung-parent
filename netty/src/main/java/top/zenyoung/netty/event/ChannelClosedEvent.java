package top.zenyoung.netty.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 设备连接通道关闭事件
 *
 * @author young
 */
@Data
@AllArgsConstructor(staticName = "of")
public class ChannelClosedEvent implements Serializable {
    /**
     * 设备ID
     */
    private String deviceId;
    /**
     * 设备IP地址
     */
    private String clientIp;
}
