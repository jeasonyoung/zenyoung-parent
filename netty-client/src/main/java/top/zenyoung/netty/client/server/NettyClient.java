package top.zenyoung.netty.client.server;

import org.springframework.boot.ApplicationRunner;

/**
 * NettyClient-客户端接口
 *
 * @author young
 */
public interface NettyClient extends ApplicationRunner {

    /**
     * 连接服务器
     */
    void connectServer();
}
