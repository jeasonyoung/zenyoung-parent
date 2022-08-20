package top.zenyoung.boot.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import top.zenyoung.boot.annotation.UserId;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

/**
 * 认证用户ID参数分解器
 *
 * @author young
 */
@Slf4j
@Component
public class UserIdMethodArgumentResolver implements ArgumentResolver {
    public static final String USER_ID = "user-id";

    @Override
    public boolean supportsParameter(@Nonnull final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserId.class);
    }

    @Override
    public Object resolveArgument(@Nonnull final MethodParameter parameter, @Nonnull final HttpServletRequest req) {
        return req.getHeader(USER_ID);
    }
}
