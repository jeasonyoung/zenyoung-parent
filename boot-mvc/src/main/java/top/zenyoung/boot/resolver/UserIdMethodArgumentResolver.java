package top.zenyoung.boot.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.WebRequest;
import top.zenyoung.boot.annotation.UserId;
import top.zenyoung.boot.util.SecurityUtils;
import top.zenyoung.common.model.UserPrincipal;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * 认证用户ID参数分解器
 *
 * @author young
 */
@Slf4j
public class UserIdMethodArgumentResolver implements ArgumentResolver {

    @Override
    public boolean supportsParameter(@Nonnull final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserId.class);
    }

    @Override
    public Object resolveArgument(@Nonnull final MethodParameter parameter, @Nonnull final WebRequest req) {
        final UserPrincipal principal = SecurityUtils.getPrincipal();
        if (Objects.nonNull(principal)) {
            log.info("获取当前用户信息: {}", principal);
            return principal.getId();
        }
        log.warn("为获取到当前用户信息");
        return null;
    }
}
