package top.zenyoung.netty.event;

import io.netty.handler.timeout.IdleState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.zenyoung.netty.session.Session;

import java.io.Serializable;

/**
 * 空闲状态事件
 *
 * @author young
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class IdleStateEvent implements Serializable {
    /**
     * 会话对象
     */
    private Session session;
    /**
     * 空闲状态
     */
    private IdleState state;
}
