package top.zenyoung.netty.event;

import io.netty.handler.timeout.IdleState;
import lombok.AllArgsConstructor;
import lombok.Data;
import top.zenyoung.netty.session.Session;

import java.io.Serializable;

/**
 * 空闲状态变更事件
 *
 * @author young
 */
@Data
@AllArgsConstructor(staticName = "of")
public class IdleStateChangeEvent implements Serializable {
    /**
     * 设备会话
     */
    private Session session;
    /**
     * 空闲状态
     */
    private IdleState state;
}
