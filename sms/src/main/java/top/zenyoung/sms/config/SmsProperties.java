package top.zenyoung.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * 短信通道配置
 *
 * @author young
 */
@Data
@ConfigurationProperties("top.zenyoung.sms")
public class SmsProperties implements Serializable {
    /**
     * 通道类型
     */
    private String type;
    /**
     * 接入账号
     */
    private String appKey;
    /**
     * 接入秘钥
     */
    private String secret;
    /**
     * 回调消息队列名称
     */
    private String callbackQueue;
    /**
     * 是否DEBUG开关
     */
    private boolean callbackQueueDebug = false;
}
