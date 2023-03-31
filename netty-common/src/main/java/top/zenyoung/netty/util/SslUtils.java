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
import java.util.Optional;

/**
 * SSL工具类
 *
 * @author young
 */
@Slf4j
public class SslUtils {
    private static SslContext sslClientContext;

    static {
        try {
            sslClientContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
        } catch (SSLException e) {
            log.error("初始化SslContext-exp: {}", e.getMessage());
        }
    }

    public static SslContext getClientContext() {
        return sslClientContext;
    }

    public static void addSslClientCodec(@Nonnull final ChannelPipeline pipeline, @Nonnull final Channel channel) {
        Optional.ofNullable(sslClientContext)
                .ifPresent(ctx -> pipeline.addLast(ctx.newHandler(channel.alloc())));
    }

    public static void addSslClientCodec(@Nonnull final ChannelPipeline pipeline) {
        Optional.ofNullable(pipeline.channel())
                .ifPresent(ch -> addSslClientCodec(pipeline, ch));
    }
}
