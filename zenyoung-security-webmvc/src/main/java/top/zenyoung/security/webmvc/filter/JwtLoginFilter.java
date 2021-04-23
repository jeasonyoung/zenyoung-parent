package top.zenyoung.security.webmvc.filter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.security.model.LoginReqBody;
import top.zenyoung.security.model.TokenAuthentication;
import top.zenyoung.security.webmvc.JwtAuthenticationManager;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Jwt登录-过滤器
 *
 * @author young
 */
@Slf4j
public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {
    private final List<RequestMatcher> requestMatchers = Lists.newLinkedList();

    /**
     * 构造函数
     *
     * @param manager 认证管理
     */
    public JwtLoginFilter(@Nonnull final JwtAuthenticationManager manager) {
        super(manager);
        this.buildRequestMatchers(this.requestMatchers);
    }

    private void buildRequestMatchers(@Nonnull final List<RequestMatcher> requestMatchers) {
        final JwtAuthenticationManager manager = (JwtAuthenticationManager) getAuthenticationManager();
        if (manager != null) {
            final List<String> loginUrls = Lists.newArrayList(manager.getLoginUrls());
            if (!CollectionUtils.isEmpty(loginUrls)) {
                requestMatchers.addAll(
                        loginUrls.stream()
                                .filter(url -> !Strings.isNullOrEmpty(url))
                                .map(AntPathRequestMatcher::new)
                                .collect(Collectors.toList())
                );
            }
        }
    }

    @Override
    protected boolean requiresAuthentication(final HttpServletRequest request, final HttpServletResponse response) {
        if (!CollectionUtils.isEmpty(requestMatchers)) {
            for (RequestMatcher m : requestMatchers) {
                if (m != null && m.matches(request)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse) throws AuthenticationException {
        final ServletServerHttpRequest httpRequest = new ServletServerHttpRequest(servletRequest);
        if (httpRequest.getMethod() != HttpMethod.POST) {
            throw new AuthenticationServiceException("Authentication method not supported: " + httpRequest.getMethod());
        }
        final MediaType contentType = httpRequest.getHeaders().getContentType();
        if (!MediaType.APPLICATION_JSON.isCompatibleWith(contentType) || !MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)) {
            throw new AuthenticationServiceException("Authentication contentType not supported: " + contentType);
        }
        //获取认证管理器
        final JwtAuthenticationManager manager = (JwtAuthenticationManager) getAuthenticationManager();
        LoginReqBody reqBody;
        if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
            try {
                reqBody = manager.parseReqBody(httpRequest.getBody(), manager.getLoginReqBodyClass());
            } catch (AuthenticationException ex) {
                log.error("attemptAuthentication-exp: {}", ex.getMessage());
                throw ex;
            } catch (Throwable ex) {
                log.error("attemptAuthentication-exp: {}", ex.getMessage());
                throw new AuthenticationException(ex.getMessage(), ex) {

                };
            }
        } else {
            final String username = obtainUsername(servletRequest);
            final String password = obtainPassword(servletRequest);
            reqBody = new LoginReqBody();
            reqBody.setAccount(Strings.isNullOrEmpty(username) ? "" : username.trim());
            reqBody.setPasswd(Strings.isNullOrEmpty(password) ? "" : password.trim());
        }
        if (reqBody == null) {
            throw new InternalAuthenticationServiceException("解析请求参数失败!");
        }
        return manager.authenticate(new TokenAuthentication(reqBody));
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain, final Authentication authResult) {
        log.debug("successfulAuthentication(chain: {},authResult: {})...", chain, authResult);
        SecurityContextHolder.getContext().setAuthentication(authResult);
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
        }
        try {
            final JwtAuthenticationManager manager = (JwtAuthenticationManager) getAuthenticationManager();
            //登录成功处理
            manager.successfulAuthenticationHandler(response, (UserPrincipal) authResult.getPrincipal());
        } catch (Throwable ex) {
            log.error("successfulAuthentication(chain: {},authResult: {})-exp: {}", chain, authResult, ex.getMessage());
            unsuccessfulAuthentication(request, response, new AuthenticationException(ex.getMessage(), ex) {
            });
        }
    }

    @Override
    protected void unsuccessfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException failed) {
        log.debug("unsuccessfulAuthentication(failed: {})...", failed == null ? null : failed.getMessage());
        SecurityContextHolder.clearContext();
        final JwtAuthenticationManager manager = (JwtAuthenticationManager) getAuthenticationManager();
        //登录失败处理
        manager.unsuccessfulAuthentication(response, failed);
    }
}
