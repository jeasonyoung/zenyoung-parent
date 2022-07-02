package top.zenyoung.netty.event;

import io.netty.handler.timeout.IdleState;
import lombok.Data;

import java.io.Serializable;

/**
 * 空闲状态事件
 *
 * @author young
 */
@Data
public class IdleStateEvent implements Serializable {
    /**
     * 空闲状态
     */
    private IdleState state;
}
