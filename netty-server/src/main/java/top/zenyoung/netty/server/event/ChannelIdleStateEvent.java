package top.zenyoung.netty.server.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.netty.event.IdleStateEvent;
import top.zenyoung.netty.session.Session;

/**
 * 空闲状态变更事件
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChannelIdleStateEvent extends IdleStateEvent {
    /**
     * 设备会话
     */
    private Session session;
}
