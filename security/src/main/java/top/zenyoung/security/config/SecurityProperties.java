package top.zenyoung.security.config;

import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.zenyoung.boot.config.CaptchaProperties;

import java.time.Duration;
import java.util.List;

/**
 * Security配置
 *
 * @author young
 */
@Data
@ConfigurationProperties("top.zenyoung.security")
public class SecurityProperties {
    /**
     * 登录URL地址
     */
    private List<String> loginUrls = Lists.newArrayList("/auth/login");
    /**
     * 免登录验证白名单
     */
    private List<String> whiteUrls = Lists.newArrayList();
    /**
     * 访问令牌有效期
     */
    private Duration accessTokenExpire = Duration.ofMinutes(10);
    /**
     * 刷新令牌有效期
     */
    private Duration refreshTokenExpire = Duration.ofMinutes(60);
    /**
     * 用户最大登录数
     */
    private Integer maxLoginTotals = 5;
    /**
     * 认证验证码图片配置
     */
    private CaptchaProperties captcha = new CaptchaProperties();
}
