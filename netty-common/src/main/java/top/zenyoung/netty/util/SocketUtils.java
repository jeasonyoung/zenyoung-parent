package top.zenyoung.netty.util;

import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import top.zenyoung.netty.handler.BaseSocketHandler;

import javax.annotation.Nonnull;

/**
 * Socket处理器工具类
 *
 * @author young
 */
@Slf4j
public class SocketUtils {

    @SuppressWarnings({"all"})
    public static ChannelHandler getHandler(@Nonnull final ApplicationContext context,
                                            @Nonnull final Class<? extends BaseSocketHandler> cls) {
        try {
            return context.getBean(cls);
        } catch (Throwable e) {
            log.error("getHandler-exp: {}", e.getMessage());
        }
        return null;
    }
}
