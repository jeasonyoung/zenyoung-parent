package top.zenyoung.security.webflux.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.zenyoung.security.model.LoginReqBody;
import top.zenyoung.security.model.TokenAuthentication;

import javax.annotation.Nonnull;
import java.util.Collections;

/**
 * 请求参数处理
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/20 6:08 下午
 **/
@Slf4j
public class ServerBodyAuthenticationConverter implements ServerAuthenticationConverter {
    private final ServerCodecConfigurer serverCodecConfigurer;
    private final ResolvableType reqLoginBodyType;

    public ServerBodyAuthenticationConverter(@Nonnull final ServerCodecConfigurer serverCodecConfigurer, @Nonnull final Class<? extends LoginReqBody> loginReqBodyClass) {
        this.serverCodecConfigurer = serverCodecConfigurer;
        this.reqLoginBodyType = ResolvableType.forClass(loginReqBodyClass);
    }

    @Override
    public Mono<Authentication> convert(final ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();
        final MediaType contentType = request.getHeaders().getContentType();
        log.debug("ServerBodyAuthenticationConverter-convert(contentType: {})", contentType);
        if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
            return serverCodecConfigurer.getReaders().stream()
                    .filter(reader -> reader.canRead(reqLoginBodyType, MediaType.APPLICATION_JSON))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No JSON reader for LoginReqBody"))
                    .readMono(reqLoginBodyType, request, Collections.emptyMap())
                    .cast(LoginReqBody.class)
                    .map(o -> new TokenAuthentication(request.getPath(), o.getAccount(), o.getPasswd(), o));
        }
        return Mono.empty();
    }
}
