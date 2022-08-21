package top.zenyoung.boot.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import top.zenyoung.boot.util.SecurityUtils;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.common.util.PrincipalUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 认证信息拦截器
 *
 * @author young
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenInterceptor implements Interceptor {
    private static final String AUTHEN_PRINCIPAL = "z-authen-principal";
    private final ObjectMapper objectMapper;

    @Override
    public void handler(@Nonnull final HttpServletRequest req, @Nonnull final HttpServletResponse res, @Nonnull final HandlerMethod handler) {
        final String base64 = req.getHeader(AUTHEN_PRINCIPAL);
        if (!Strings.isNullOrEmpty(base64)) {
            try {
                final UserPrincipal principal = PrincipalUtils.decode(objectMapper, base64);
                if (Objects.nonNull(principal)) {
                    SecurityUtils.setAuthentication(principal);
                }
            } catch (Throwable e) {
                log.warn("拦截认证信息失败: {}", e.getMessage());
            }
        }
    }
}
