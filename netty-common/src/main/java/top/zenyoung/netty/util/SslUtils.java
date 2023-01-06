package top.zenyoung.netty.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLException;
import java.util.Objects;

/**
 * SSL工具类
 *
 * @author young
 */
@Slf4j
public class SslUtils {
    private static SslContext sslContext;

    static {
        try {
            sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
        } catch (SSLException e) {
            log.error("初始化SslContext-exp: {}", e.getMessage());
        }
    }

    public static SslContext getContext() {
        return sslContext;
    }

    public static void addSslCodec(@Nonnull final ChannelPipeline pipeline, @Nonnull final Channel channel) {
        if (Objects.nonNull(sslContext)) {
            pipeline.addLast(sslContext.newHandler(channel.alloc()));
        }
    }

}
