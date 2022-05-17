package top.zenyoung.framework.auth;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import top.zenyoung.framework.captcha.CaptchaProperties;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;

/**
 * 认证配置
 *
 * @author young
 */
@Data
public class AuthProperties implements Serializable {
    /**
     * 登录URL地址
     */
    private List<String> loginUrls = Lists.newArrayList("/auth/login");
    /**
     * 免登录验证白名单
     */
    private List<String> whiteUrls = Lists.newArrayList();
    /**
     * 令牌有效期
     */
    private Duration tokenExpire = Duration.ofMinutes(10);
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

    /**
     * 获取白名单数组
     *
     * @return 白名单数组
     */
    public String[] getWhiteLists() {
        if (CollectionUtils.isEmpty(whiteUrls)) {
            return new String[0];
        }
        return whiteUrls.stream()
                .filter(w -> !Strings.isNullOrEmpty(w))
                .toArray(String[]::new);
    }
}
