package top.zenyoung.framework.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.zenyoung.security.webmvc.BaseMvcAuthenticationManager;

import javax.annotation.Nonnull;

/**
 * 认证管理器服务基类
 *
 * @author young
 */
public abstract class BaseAuthenticationManagerService extends BaseMvcAuthenticationManager<LoginDTO> {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuthProperties authConfig;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Nonnull
    @Override
    public String[] getLoginUrls() {
        return authConfig.getLoginUrls().stream()
                .filter(url -> !Strings.isNullOrEmpty(url))
                .toArray(String[]::new);
    }

    @Override
    public String[] getWhiteUrls() {
        return authConfig.getWhiteLists();
    }

    @Nonnull
    @Override
    protected ObjectMapper getObjMapper() {
        return objectMapper;
    }

    @Nonnull
    @Override
    public Class<LoginDTO> getLoginReqBodyClass() {
        return LoginDTO.class;
    }

    @Nonnull
    @Override
    protected PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
