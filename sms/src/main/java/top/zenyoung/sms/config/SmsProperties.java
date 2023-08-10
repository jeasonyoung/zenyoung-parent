package top.zenyoung.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.util.Properties;

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
    private String type = "ali";
    /**
     * 接入账号
     */
    private String appKey;
    /**
     * 接入秘钥
     */
    private String secret;
    /**
     * 扩展属性集合
     */
    private Properties extend = new Properties();
}
