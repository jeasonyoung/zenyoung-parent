package top.zenyoung.security.webflux;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import top.zenyoung.security.spi.token.TokenService;

import javax.annotation.Nonnull;

/**
 * JWT-令牌认证-拦截器
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/1/7 11:00 下午
 **/
@Slf4j
public class JwtTokenWebFilter implements WebFilter, AuthFilter {

    /**
     * 构造函数
     *
     * @param tokenService 令牌服务
     * @param objectMapper Json处理器
     */
    public JwtTokenWebFilter(@Nonnull final TokenService tokenService, @Nonnull final ObjectMapper objectMapper) {

    }

    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain) {
        return null;
    }
}
