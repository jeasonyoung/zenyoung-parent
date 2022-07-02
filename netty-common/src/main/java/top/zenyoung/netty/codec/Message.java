package top.zenyoung.netty.codec;

import java.io.Serializable;

/**
 * 消息对象接口
 *
 * @author young
 */
public interface Message extends Serializable {
    /**
     * 获取消息发送设备ID
     *
     * @return 设备ID
     */
    String getDeviceId();

    /**
     * 获取消息执行指令
     *
     * @return 消息指令
     */
    String getCommand();
}
