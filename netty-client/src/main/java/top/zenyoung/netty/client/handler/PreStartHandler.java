package top.zenyoung.netty.client.handler;

import org.springframework.boot.ApplicationArguments;

import javax.annotation.Nullable;

/**
 * 启动前置处理器
 *
 * @author young
 */
public interface PreStartHandler {

    /**
     * 启动前置处理
     *
     * @param args 启动参数
     */
    void preHandler(@Nullable final ApplicationArguments args);
}
