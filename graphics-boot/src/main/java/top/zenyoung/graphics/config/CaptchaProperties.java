package top.zenyoung.graphics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.zenyoung.graphics.model.CaptchaCategory;
import top.zenyoung.graphics.model.CaptchaType;

import java.io.Serializable;
import java.util.Properties;

/**
 * 认证验证码图片配置
 *
 * @author young
 */
@Data
@ConfigurationProperties("top.zenyoung.captcha")
public class CaptchaProperties implements Serializable {
    /**
     * 是否启用图像验证
     */
    private Boolean enable = false;
    /**
     * 验证码类别
     */
    private CaptchaCategory category = CaptchaCategory.Gif;
    /**
     * 验证码类型
     */
    private CaptchaType type = CaptchaType.Math;
    /**
     * 图形宽度
     */
    private Integer width = 160;
    /**
     * 图形高度
     */
    private Integer height = 60;
    /**
     * 验证码属性配置
     */
    private Properties properties = new Properties();
}