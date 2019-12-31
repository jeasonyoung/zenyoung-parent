package top.zenyoung.security.webflux;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.zenyoung.common.response.RespResult;
import top.zenyoung.common.response.ResultCode;
import top.zenyoung.security.spi.TokenUserDetail;
import top.zenyoung.security.spi.auth.ReqLoginBody;
import top.zenyoung.security.spi.auth.RespLoginBody;
import top.zenyoung.security.spi.token.TokenAuthentication;
import top.zenyoung.webflux.util.RespUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 登录-过滤器
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 8:44 下午
 **/
@Slf4j
public class LoginWebFilter extends AuthenticationWebFilter implements AuthFilter {
    /**
     * 构造函数
     *
     * @param authenticationManager 认证管理器
     * @param serverCodecConfigurer 服务端解码配置
     * @param objectMapper          Json处理器
     */
    public LoginWebFilter(@Nonnull final AuthenticationManager authenticationManager, @Nonnull final ServerCodecConfigurer serverCodecConfigurer, @Nonnull final ObjectMapper objectMapper) {
        super(authenticationManager);
        //登录地址
        setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, getAuthLoginUrls()));
        //登录请求参数解析
        setServerAuthenticationConverter(new ServerBodyAuthenticationConverter(authenticationManager, serverCodecConfigurer));
        //登录成功处理
        setAuthenticationSuccessHandler(new AuthenticationSuccessHandler(authenticationManager, objectMapper));
        //登录失败处理
        setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler((exchange, e) -> {
            String err = e.getMessage();
            if (Strings.isNullOrEmpty(err)) {
                err = "账号或密码错误";
            }
            return RespUtils.buildResponse(exchange.getResponse(), objectMapper, 401, err);
        }));
    }

    /**
     * 获取请求头字段值集合
     *
     * @param request 请求
     * @param manager 认证管理器
     * @return 请求头字段值集合
     */
    private static Map<String, List<String>> getHeaderValues(@Nonnull final ServerHttpRequest request, @Nonnull final AuthenticationManager manager) {
        //请求头处理
        final AuthenticationManager.RequestHeaderHandler hander = manager.checkRequestHeaders();
        if (hander != null) {
            final String[] reqHeaders = hander.getHeaderNames();
            if (reqHeaders != null && reqHeaders.length > 0) {
                final HttpHeaders headers = request.getHeaders();
                if (!CollectionUtils.isEmpty(headers)) {
                    return Stream.of(reqHeaders)
                            .filter(reqHeader -> !Strings.isNullOrEmpty(reqHeader))
                            .collect(Collectors.toMap(name -> name, headers::getOrEmpty, (o, n) -> n));
                }
            }
        }
        return null;
    }

    /**
     * 认证请求报文体转换器
     */
    private static class ServerBodyAuthenticationConverter implements ServerAuthenticationConverter {
        private final ResolvableType reqBodyType = ResolvableType.forClass(ReqLoginBody.class);
        private final AuthenticationManager authenticationManager;
        private final ServerCodecConfigurer serverCodecConfigurer;

        private ServerBodyAuthenticationConverter(@Nonnull final AuthenticationManager authenticationManager, @Nonnull final ServerCodecConfigurer serverCodecConfigurer) {
            this.authenticationManager = authenticationManager;
            this.serverCodecConfigurer = serverCodecConfigurer;
        }

        @Override
        public Mono<Authentication> convert(final ServerWebExchange exchange) {
            final ServerHttpRequest request = exchange.getRequest();
            final MediaType contentType = request.getHeaders().getContentType();
            log.info("ServerBodyAuthenticationConverter-convert(contentType: {})", contentType);
            if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
                return serverCodecConfigurer.getReaders().stream()
                        .filter(reader -> reader.canRead(reqBodyType, MediaType.APPLICATION_JSON))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No JSON reader for ReqAuthLogin"))
                        .readMono(reqBodyType, request, Collections.emptyMap())
                        .cast(ReqLoginBody.class)
                        .map(o -> {
                            //生成认证令牌数据
                            final TokenAuthentication token = new TokenAuthentication(o.getAccount(), o.getPasswd(), o.getType());
                            //请求头字段集合
                            final Map<String, List<String>> headerValues = getHeaderValues(request, authenticationManager);
                            if (!CollectionUtils.isEmpty(headerValues)) {
                                final AuthenticationManager.RequestHeaderHandler hander = authenticationManager.checkRequestHeaders();
                                if (hander != null) {
                                    try {
                                        hander.beforeAuthenHandler(headerValues, token);
                                    } catch (Throwable ex) {
                                        log.error("convert-beforeAuthenHandler(headerValues: {},token: {})-exp: {}", headerValues, token, ex.getMessage());
                                        throw new RuntimeException(ex);
                                    }
                                }
                            }
                            return token;
                        });
            }
            return Mono.empty();
        }
    }

    /**
     * 认证成功处理
     */
    private static class AuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {
        private final AuthenticationManager authenticationManager;
        private final ObjectMapper objectMapper;

        private AuthenticationSuccessHandler(@Nonnull final AuthenticationManager authenticationManager, @Nonnull final ObjectMapper objectMapper) {
            this.authenticationManager = authenticationManager;
            this.objectMapper = objectMapper;
        }

        @Override
        public Mono<Void> onAuthenticationSuccess(final WebFilterExchange webFilterExchange, final Authentication authen) {
            log.info("onAuthenticationSuccess(authen: {})...", authen);
            if (authen.getPrincipal() instanceof TokenUserDetail) {
                final ServerWebExchange exchange = webFilterExchange.getExchange();
                final TokenUserDetail tokenUserDetail = (TokenUserDetail) authen.getPrincipal();
                //请求头字段集合
                final Map<String, List<String>> headerValues = getHeaderValues(exchange.getRequest(), authenticationManager);
                if (!CollectionUtils.isEmpty(headerValues)) {
                    final AuthenticationManager.RequestHeaderHandler hander = authenticationManager.checkRequestHeaders();
                    if (hander != null) {
                        try {
                            hander.afterAuthenHandler(headerValues, tokenUserDetail);
                        } catch (Throwable ex) {
                            log.error("onAuthenticationSuccess-afterAuthenHandler(headerValues: {},tokenUserDetail: {})-exp: {}", headerValues, tokenUserDetail, ex.getMessage());
                            return Mono.error(ex);
                        }
                    }
                }
                //登录成功结果数据处理
                return RespUtils.buildResponse(exchange.getResponse(), objectMapper, buildRespResult(tokenUserDetail));
            }
            return Mono.error(new IllegalArgumentException("authen.getPrincipal()不能转换为TokenUserDetail=>" + authen.getPrincipal()));
        }

        private RespResult<RespLoginBody> buildRespResult(@Nonnull final TokenUserDetail data) {
            final RespResult<RespLoginBody> respResult = RespResult.<RespLoginBody>builder().build().buildResult(ResultCode.Success);
            try {
                respResult.setData(authenticationManager.getUserResp(data));
            } catch (Throwable ex) {
                respResult.buildResult(ex);
                log.error("buildRespResult(data: {})-exp: {}", data, ex.getMessage());
            }
            return respResult;
        }
    }
}
