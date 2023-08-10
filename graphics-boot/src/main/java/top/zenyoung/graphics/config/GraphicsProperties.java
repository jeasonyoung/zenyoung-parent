package top.zenyoung.graphics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.util.Properties;

/**
 * 配置属性
 *
 * @author young
 */
@Data
@ConfigurationProperties(prefix = "top.zenyoung.graphics")
public class GraphicsProperties {
    /**
     * 验证码配置
     */
    private Captcha captcha = new Captcha();

    /**
     * 验证码配置
     */
    @Data
    public static class Captcha implements Serializable {
        /**
         * 验证码类别
         */
        private String category = "GIF";
        /**
         * 验证码类型
         */
        private String type = "MATH";
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
}
