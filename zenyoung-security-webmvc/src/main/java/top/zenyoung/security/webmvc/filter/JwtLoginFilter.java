package top.zenyoung.security.webmvc.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.ServletServerHttpRequest;
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
import top.zenyoung.security.model.LoginRespBody;
import top.zenyoung.security.model.TokenAuthentication;
import top.zenyoung.security.webmvc.ZyAuthenticationManager;
import top.zenyoung.web.controller.util.RespJsonUtils;
import top.zenyoung.web.vo.RespResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    private final ZyAuthenticationManager manager;
    private final ObjectMapper objectMapper;

    public JwtLoginFilter(@Nonnull final ZyAuthenticationManager manager, @Nullable final ObjectMapper objectMapper) {
        super(manager);
        this.manager = manager;
        this.objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;
        this.buildRequestMatchers(this.requestMatchers, this.manager);
    }

    private void buildRequestMatchers(@Nonnull final List<RequestMatcher> requestMatchers, @Nonnull final ZyAuthenticationManager manager) {
        final HttpMethod method = manager.getLoginMethod();
        final List<String> loginUrls = Lists.newArrayList(manager.getLoginUrls());
        if (!CollectionUtils.isEmpty(loginUrls)) {
            requestMatchers.addAll(
                    loginUrls.stream()
                            .filter(url -> !Strings.isNullOrEmpty(url))
                            .map(url -> new AntPathRequestMatcher(url, method.name()))
                            .collect(Collectors.toList())
            );
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
        if (MediaType.APPLICATION_JSON.isCompatibleWith(httpRequest.getHeaders().getContentType())) {
            try {
                final LoginReqBody reqBody = objectMapper.readValue(httpRequest.getBody(), manager.getLoginReqBodyClass());
                if (reqBody != null) {
                    final RequestPath requestPath = RequestPath.parse(servletRequest.getRequestURI(), servletRequest.getContextPath());
                    final TokenAuthentication tokenAuthentication = new TokenAuthentication(requestPath, reqBody.getAccount(), reqBody.getPasswd(), reqBody);
                    return manager.authenticate(tokenAuthentication);
                }
            } catch (AuthenticationException ex) {
                log.error("attemptAuthentication-exp: {}", ex.getMessage());
                throw ex;
            } catch (Throwable ex) {
                log.error("attemptAuthentication-exp: {}", ex.getMessage());
                throw new AuthenticationException(ex.getMessage(), ex) {

                };
            }
        }
        return super.attemptAuthentication(servletRequest, servletResponse);
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain, final Authentication authResult) throws IOException, ServletException {
        log.debug("successfulAuthentication(chain: {},authResult: {})...", chain, authResult);
        SecurityContextHolder.getContext().setAuthentication(authResult);
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
        }
        try {
            //登录成功处理
            final UserPrincipal principal = (UserPrincipal) authResult.getPrincipal();
            if (principal != null) {
                final LoginRespBody respBody = this.manager.createRespBody(principal);
                RespJsonUtils.buildSuccessResp(objectMapper, response, respBody);
            }
        } catch (Throwable ex) {
            log.error("successfulAuthentication(chain: {},authResult: {})-exp: {}", chain, authResult, ex.getMessage());
            unsuccessfulAuthentication(request, response, new AuthenticationException(ex.getMessage(), ex) {
            });
        }
    }

    @Override
    protected void unsuccessfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException failed) throws IOException, ServletException {
        log.debug("unsuccessfulAuthentication(failed: {})...", failed == null ? null : failed.getMessage());
        SecurityContextHolder.clearContext();
        RespJsonUtils.buildFailResp(objectMapper, response, HttpStatus.UNAUTHORIZED, failed);
    }
}
