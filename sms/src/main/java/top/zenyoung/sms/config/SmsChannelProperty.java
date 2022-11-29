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
public class SmsChannelProperty implements Serializable {
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
}
