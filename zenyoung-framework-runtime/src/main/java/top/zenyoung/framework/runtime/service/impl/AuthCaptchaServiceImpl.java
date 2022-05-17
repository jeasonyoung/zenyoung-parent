package top.zenyoung.framework.runtime.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.zenyoung.framework.auth.AuthProperties;
import top.zenyoung.framework.service.AuthCaptchaService;

import javax.annotation.Nonnull;

/**
 * 认证验证码-服务接口实现
 *
 * @author young
 */
@Service
@RequiredArgsConstructor
public class AuthCaptchaServiceImpl implements AuthCaptchaService {
    private final AuthProperties authProperties;

    @Override
    public AuthCaptcha createCaptcha() {
        ///TODO:
        return null;
    }

    @Override
    public boolean verify(@Nonnull Long captchaId, String captchaCode) {
        return false;
    }
}
