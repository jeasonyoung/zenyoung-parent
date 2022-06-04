package top.zenyoung.framework.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;
import top.zenyoung.framework.captcha.CaptchaProperties;
import top.zenyoung.framework.service.AuthCaptchaService;
import top.zenyoung.security.model.TokenAuthentication;
import top.zenyoung.security.webmvc.BaseMvcAuthenticationManager;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * 认证管理器服务基类
 *
 * @author young
 */
public abstract class BaseAuthenticationManagerService extends BaseMvcAuthenticationManager<LoginDTO> {
    @Autowired
    protected ApplicationContext context;

    /**
     * 获取认证配置属性
     *
     * @return 认证配置属性
     */
    protected AuthProperties getAuthProperties() {
        return context.getBean(AuthProperties.class);
    }

    @Nonnull
    @Override
    public String[] getLoginUrls() {
        final AuthProperties authProperties = getAuthProperties();
        final List<String> loginUrls;
        if (Objects.nonNull(authProperties) && !CollectionUtils.isEmpty(loginUrls = authProperties.getLoginUrls())) {
            return loginUrls.stream()
                    .filter(url -> !Strings.isNullOrEmpty(url))
                    .toArray(String[]::new);
        }
        return new String[0];
    }

    @Override
    public String[] getWhiteUrls() {
        final AuthProperties authProperties = getAuthProperties();
        final List<String> whiteUrls;
        if (Objects.nonNull(authProperties) && !CollectionUtils.isEmpty(whiteUrls = authProperties.getWhiteUrls())) {
            return whiteUrls.stream()
                    .filter(url -> !Strings.isNullOrEmpty(url))
                    .toArray(String[]::new);
        }
        return new String[0];
    }

    @Nonnull
    @Override
    public ObjectMapper getObjMapper() {
        return context.getBean(ObjectMapper.class);
    }

    @Nonnull
    @Override
    public Class<LoginDTO> getLoginReqBodyClass() {
        return LoginDTO.class;
    }

    @Nonnull
    @Override
    protected PasswordEncoder getPasswordEncoder() {
        return context.getBean(PasswordEncoder.class);
    }

    @Override
    public TokenAuthentication<LoginDTO> buildBeforeAuthenticate(@Nonnull final ServletServerHttpRequest request, @Nonnull final LoginDTO reqBody) {
        final AuthProperties authProperties = getAuthProperties();
        final CaptchaProperties captchaProperties;
        if (Objects.nonNull(authProperties) && Objects.nonNull(captchaProperties = authProperties.getCaptcha()) && captchaProperties.getEnable()) {
            //检查验证码ID及用户输入验证码
            final Long captchaId = reqBody.getVerifyId();
            final String inputCaptchaCode = reqBody.getVerifyCode();
            if (Objects.isNull(captchaId) || Strings.isNullOrEmpty(inputCaptchaCode)) {
                throw new BadCredentialsException("请输入验证码");
            }
            final AuthCaptchaService captchaService = context.getBean(AuthCaptchaService.class);
            //校验图形验证码
            if (!captchaService.verify(captchaId, inputCaptchaCode)) {
                throw new BadCredentialsException("验证码不正确");
            }
        }
        return super.buildBeforeAuthenticate(request, reqBody);
    }
}
