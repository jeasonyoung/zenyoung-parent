package top.zenyoung.security.webflux.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.controller.utl.RespJsonUtils;
import top.zenyoung.security.model.LoginRespBody;
import top.zenyoung.security.webflux.AuthenticationManager;
import top.zenyoung.security.webflux.converter.ServerBodyAuthenticationConverter;
import top.zenyoung.web.vo.RespResult;

import javax.annotation.Nonnull;

/**
 * Jwt登录-过滤器
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/20 5:56 下午
 **/
@Slf4j
public class JwtLoginFilter extends AuthenticationWebFilter {

    public JwtLoginFilter(@Nonnull final AuthenticationManager authenticationManager, @Nonnull final ServerCodecConfigurer serverCodecConfigurer) {
        super(authenticationManager);
        //设置登录地址
        setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers(authenticationManager.getLoginMethod(), authenticationManager.getLoginUrls()));
        //登录请求参数解析
        setServerAuthenticationConverter(new ServerBodyAuthenticationConverter(serverCodecConfigurer, authenticationManager.getLoginReqBodyClass()));
        //登录成功处理
        setAuthenticationSuccessHandler((filterExchange, authen) -> {
            log.debug("setAuthenticationSuccessHandler(authen: {})...", authen);
            if (authen.getPrincipal() instanceof UserPrincipal) {
                try {
                    final ServerWebExchange exchange = filterExchange.getExchange();
                    //获取登录用户数据
                    final UserPrincipal principal = new UserPrincipal((UserPrincipal) authen.getPrincipal());
                    //构建响应数据
                    final LoginRespBody respBody = authenticationManager.createRespBody(principal);
                    //构建登录用户数据
                    authenticationManager.buildRespBody(respBody, principal);
                    //构建响应数据
                    return RespJsonUtils.buildSuccessResp(
                            exchange.getResponse(),
                            RespResult.ofSuccess(respBody)
                    );
                } catch (Throwable ex) {
                    log.debug("setAuthenticationSuccessHandler(authen: {})-exp: {}", authen, ex.getMessage());
                    return Mono.error(ex);
                }
            }
            return Mono.error(new IllegalArgumentException("authen.getPrincipal()不能转换为UserPrincipal=>" + authen.getPrincipal()));
        });
        //登录失败处理
        setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(
                (exchange, e) -> RespJsonUtils.buildFailResp(exchange.getResponse(), HttpStatus.UNAUTHORIZED, e))
        );
    }
}