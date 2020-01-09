package top.zenyoung.security.webflux;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.zenyoung.security.spi.TokenUserDetail;
import top.zenyoung.security.spi.token.TokenAuthentication;
import top.zenyoung.security.spi.token.TokenDetail;
import top.zenyoung.security.spi.token.TokenService;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 令牌认证数据转换
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/1/9 9:11 下午
 **/
@Slf4j
public class TokenAuthenticationConverter implements ServerAuthenticationConverter {
    private static final String HEADER_TOKEN = HttpHeaders.AUTHORIZATION;
    private final TokenService tokenService;

    /**
     * 构造函数
     *
     * @param tokenService 令牌服务
     */
    public TokenAuthenticationConverter(@Nonnull final TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Mono<Authentication> convert(final ServerWebExchange exchange) {
        if (exchange == null) {
            return Mono.empty();
        }
        final ServerHttpRequest request = exchange.getRequest();
        //获取认证令牌数据
        final String token = request.getHeaders().getFirst(HEADER_TOKEN);
        if (Strings.isNullOrEmpty(token)) {
            return Mono.empty();
        }
        try {
            //解析令牌数据
            final TokenDetail detail = tokenService.parseToken(token);
            //当前用户角色集合
            List<GrantedAuthority> authorities = Lists.newLinkedList();
            if (!CollectionUtils.isEmpty(detail.getRoles())) {
                authorities = detail.getRoles().stream()
                        .filter(role -> !Strings.isNullOrEmpty(role))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }
            //当前请求URL
            final String url = request.getPath().value();
            log.info("token={}\nurl={}\nauthorities:{}", token, url, authorities);
            ///TODO: 检查请求授权处理ß
            return Mono.just(new TokenAuthentication(new TokenUserDetail(detail), null, authorities, detail.getType()));
        } catch (Throwable ex) {
            log.warn("convert(token: {})-exp: {}", token, ex.getMessage());
            return Mono.error(ex);
        }
    }
}
