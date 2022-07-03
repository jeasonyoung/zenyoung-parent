package top.zenyoung.netty;

import com.google.common.base.Strings;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.LogLevel;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.netty.prop.BaseProperties;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.Objects;

/**
 * Netty实现基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseNettyImpl<T extends BaseProperties> implements Runnable, Closeable {
    protected static final boolean IS_EPOLL;
    protected static final EventLoopGroup BOSS_GROUP;
    protected static final EventLoopGroup WORKER_GROUP;

    static {
        final int bossSize = Math.max(Runtime.getRuntime().availableProcessors() * 2, 2);
        final int workSize = bossSize * 2;

        IS_EPOLL = Epoll.isAvailable();
        BOSS_GROUP = IS_EPOLL ? new EpollEventLoopGroup(bossSize) : new NioEventLoopGroup(bossSize);
        WORKER_GROUP = IS_EPOLL ? new EpollEventLoopGroup(workSize) : new NioEventLoopGroup(workSize);
    }

    /**
     * 获取配置数据
     *
     * @return 配置数据
     */
    protected abstract T getProperties();

    /**
     * 获取日志级别
     *
     * @return 日志级别
     */
    protected LogLevel getLogLevel() {
        final String level;
        if (!Strings.isNullOrEmpty(level = this.getProperties().getLogLevel())) {
            try {
                return Enum.valueOf(LogLevel.class, level.toUpperCase());
            } catch (Throwable e) {
                log.warn("getLogLevel(level: {})-exp: {}", level, e.getMessage());
            }
        }
        return LogLevel.DEBUG;
    }

    @Override
    public final void run() {
        try {
            log.info("Netty启动...");
            final Integer port = this.getProperties().getPort();
            if (Objects.isNull(port) || port <= 0) {
                log.error("Netty-未配置服务器监听端口!");
                return;
            }
            //启动
            this.start(port);
        } catch (Throwable e) {
            log.error("Netty运行失败: {}", e.getMessage());
        }
    }

    /**
     * 启动netty
     *
     * @param port 端口
     */
    protected abstract void start(@Nonnull final Integer port);
}
