package top.zenyoung.netty.handler;

import io.netty.handler.timeout.IdleStateHandler;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 心跳处理器
 *
 * @author young
 */
public class HeartbeatHandler extends IdleStateHandler {

    /**
     * 构造函数
     *
     * @param heartbeat 心跳时间
     */
    public HeartbeatHandler(@Nonnull final Duration heartbeat) {
        super(heartbeat.toMillis(), heartbeat.toMillis(), heartbeat.toMillis(), TimeUnit.MILLISECONDS);
    }
}
