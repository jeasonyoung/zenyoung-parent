package top.zenyoung.boot.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;
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
    public Mono<Object> resolveArgument(@Nonnull final MethodParameter parameter, @Nonnull final ServerHttpRequest req) {
        return SecurityUtils.getPrincipal()
                .filter(Objects::nonNull)
                .map(UserPrincipal::getId);
    }
}
