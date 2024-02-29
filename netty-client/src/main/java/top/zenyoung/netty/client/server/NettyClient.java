package top.zenyoung.netty.client.server;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationRunner;

/**
 * NettyClient-客户端接口
 *
 * @author young
 */
public interface NettyClient extends ApplicationRunner, DisposableBean {
    
}
