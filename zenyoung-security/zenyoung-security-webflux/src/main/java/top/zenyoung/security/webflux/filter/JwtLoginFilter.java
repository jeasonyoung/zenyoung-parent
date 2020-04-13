package top.zenyoung.security.webflux.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.zenyoung.common.model.RespResult;
import top.zenyoung.security.model.LoginRespBody;
import top.zenyoung.security.model.UserPrincipal;
import top.zenyoung.security.webflux.AuthenticationManager;
import top.zenyoung.security.webflux.converter.ServerBodyAuthenticationConverter;
import top.zenyoung.security.webflux.model.TokenUserDetails;
import top.zenyoung.security.webflux.util.RespJsonUtils;

import javax.annotation.Nonnull;

/**
 * Jwt登录-过滤器
 *
 * @author yangyong
 * @version 1.0
 *  2020/3/20 5:56 下午
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
            log.info("setAuthenticationSuccessHandler(authen: {})...", authen);
            if (authen.getPrincipal() instanceof TokenUserDetails) {
                final ServerWebExchange exchange = filterExchange.getExchange();
                final TokenUserDetails userDetails = (TokenUserDetails) authen.getPrincipal();
                final UserPrincipal principal = new UserPrincipal();
                //用户ID
                principal.setId(userDetails.getId());
                //用户账号
                principal.setAccount(userDetails.getAccount());
                //用户角色集合
                principal.setRoles(userDetails.getRoles());
                //构建响应数据
                return RespJsonUtils.buildSuccessResp(
                        exchange.getResponse(),
                        RespResult.<LoginRespBody>builder().build().buildRespSuccess(authenticationManager.getUserResp(principal))
                );
            }
            return Mono.error(new IllegalArgumentException("authen.getPrincipal()不能转换为UserPrincipal=>" + authen.getPrincipal()));
        });
        //登录失败处理
        setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(
                (exchange, e) -> RespJsonUtils.buildFailResp(exchange.getResponse(), HttpStatus.UNAUTHORIZED, e))
        );
    }
}