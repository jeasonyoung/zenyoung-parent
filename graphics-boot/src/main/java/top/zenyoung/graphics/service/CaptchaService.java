package top.zenyoung.graphics.service;

import top.zenyoung.common.vo.CaptchaVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;

/**
 * 验证码-服务接口
 *
 * @author young
 */
public interface CaptchaService {

    /**
     * 创建验证码数据
     *
     * @param len    验证码长度
     * @param expire 有效期
     * @return 验证码数据
     */
    CaptchaVO createCaptcha(@Nullable final Integer len, @Nullable final Duration expire);

    /**
     * 校验验证码
     *
     * @param captchaId 验证码ID
     * @param inputCode 输入验证码值
     * @return 校验结果
     */
    boolean verify(@Nonnull final Long captchaId, @Nonnull final String inputCode);
}
