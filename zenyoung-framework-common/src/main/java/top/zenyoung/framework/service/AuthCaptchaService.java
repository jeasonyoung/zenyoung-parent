package top.zenyoung.framework.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;

/**
 * 认证验证码-服务接口
 *
 * @author young
 */
public interface AuthCaptchaService {

    /**
     * 创建验证码数据
     *
     * @return 验证码数据
     */
    AuthCaptcha createCaptcha();

    /**
     * 校验验证码
     *
     * @param captchaId 验证码ID
     * @param inputCode 输入验证码值
     * @return 校验结果
     */
    boolean verify(@Nonnull final Long captchaId, @Nonnull final String inputCode);

    /**
     * 认证验证码
     */
    @Data
    @RequiredArgsConstructor(staticName = "of")
    class AuthCaptcha {
        /**
         * 验证码ID
         */
        private final Long captchaId;
        /**
         * 验证码图片(base64)
         */
        private final String base64Data;
    }
}
