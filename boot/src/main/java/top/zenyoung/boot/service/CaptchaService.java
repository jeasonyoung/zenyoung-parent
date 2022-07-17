package top.zenyoung.boot.service;

import top.zenyoung.common.vo.CaptchaVO;

import javax.annotation.Nonnull;

/**
 * 验证码-服务接口
 *
 * @author young
 */
public interface CaptchaService {

    /**
     * 创建验证码数据
     *
     * @return 验证码数据
     */
    CaptchaVO createCaptcha();

    /**
     * 校验验证码
     *
     * @param captchaId 验证码ID
     * @param inputCode 输入验证码值
     * @return 校验结果
     */
    boolean verify(@Nonnull final Long captchaId, @Nonnull final String inputCode);
}
