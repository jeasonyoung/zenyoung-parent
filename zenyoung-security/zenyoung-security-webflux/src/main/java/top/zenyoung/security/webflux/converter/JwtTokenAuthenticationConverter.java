package top.zenyoung.security.webflux.converter;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.zenyoung.security.token.TokenTicket;
import top.zenyoung.security.webflux.AuthenticationManager;
import top.zenyoung.security.webflux.model.TokenAuthentication;
import top.zenyoung.security.webflux.model.TokenUserDetails;

import javax.annotation.Nonnull;

/**
 * Jwt令牌认证转换器类
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/3/19 4:02 下午
 **/
@Slf4j
public class JwtTokenAuthenticationConverter implements ServerAuthenticationConverter {
    private static final String TOKEN_HEADER_NAME = HttpHeaders.AUTHORIZATION;
    private final AuthenticationManager authenticationManager;

    public JwtTokenAuthenticationConverter(@Nonnull final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Mono<Authentication> convert(@Nonnull final ServerWebExchange exchange) {
        //获取令牌
        final ServerHttpRequest request = exchange.getRequest();
        final String authorization = request.getHeaders().getFirst(TOKEN_HEADER_NAME);
        if (Strings.isNullOrEmpty(authorization)) {
            //令牌为空
            return Mono.empty();
        }
        try {
            //解析令牌
            final TokenTicket ticket = authenticationManager.getToken().parseToken(authorization);
            final TokenUserDetails userDetails = new TokenUserDetails(ticket);
            //转换用户数据
            return Mono.just(new TokenAuthentication(userDetails, null, userDetails.getAuthorities()));
        } catch (Throwable ex) {
            log.warn("convert(authorization: " + authorization + ")-exp:" + ex.getMessage());
            return Mono.error(new AuthenticationException(ex.getMessage(), ex) {
            });
        }
    }
}