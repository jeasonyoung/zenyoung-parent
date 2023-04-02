package top.zenyoung.netty.mbean;

import java.io.Serializable;

/**
 * 流量接收机MBean
 *
 * @author young
 */
public interface TrafficAcceptorMBean extends Serializable {
    /**
     * netty写流量 bytes/s 对浏览器来说其实就是下载速度
     *
     * @return 下载速度
     */
    long getWrittenBytesThroughput();

    /**
     * netty 读流量 bytes/s 对浏览器来说其实就是上传速度
     *
     * @return 上传速度
     */
    long getReadBytesThroughput();
}
