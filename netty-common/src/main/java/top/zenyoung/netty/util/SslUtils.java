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
        if (Objects.nonNull(sslClientContext)) {
            pipeline.addLast(sslClientContext.newHandler(channel.alloc()));
        }
    }

    public static void addSslClientCodec(@Nonnull final ChannelPipeline pipeline){
        final Channel channel = pipeline.channel();
        if(Objects.nonNull(channel)){
            addSslClientCodec(pipeline, channel);
        }
    }
}
