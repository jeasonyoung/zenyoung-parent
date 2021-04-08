package top.zenyoung.security.webmvc.filter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.RequestPath;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import top.zenyoung.security.exception.TokenException;
import top.zenyoung.security.model.TokenAuthentication;
import top.zenyoung.security.model.TokenUserDetails;
import top.zenyoung.security.token.Ticket;
import top.zenyoung.security.webmvc.AuthenticationManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Jwt令牌认证-过滤器
 *
 * @author young
 */
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {
    private static final String TOKEN_HEADER_NAME = HttpHeaders.AUTHORIZATION;
    private final List<RequestMatcher> whiteMatchers = Lists.newLinkedList();
    private final AuthenticationManager manager;

    public JwtTokenFilter(@Nonnull final AuthenticationManager manager) {
        this.manager = manager;
        this.buildWhiteUrls(whiteMatchers, manager);
    }

    private void buildWhiteUrls(@Nonnull final List<RequestMatcher> whiteMatchers, @Nonnull final AuthenticationManager authenticationManager) {
        final List<String> whiteUrls = Lists.newLinkedList();
        //用户登录
        final String[] loginUrls = authenticationManager.getLoginUrls();
        if (loginUrls.length > 0) {
            whiteUrls.addAll(Arrays.stream(loginUrls)
                    .filter(val -> !Strings.isNullOrEmpty(val))
                    .collect(Collectors.toList())
            );
        }
        //白名单
        final String[] urls = authenticationManager.getWhiteUrls();
        if (urls != null && urls.length > 0) {
            whiteUrls.addAll(Arrays.stream(urls)
                    .filter(url -> !Strings.isNullOrEmpty(url))
                    .collect(Collectors.toList())
            );
        }
        if (!CollectionUtils.isEmpty(whiteUrls)) {
            whiteMatchers.addAll(whiteUrls.stream()
                    .filter(url -> !Strings.isNullOrEmpty(url))
                    .distinct()
                    .map(AntPathRequestMatcher::new)
                    .collect(Collectors.toList())
            );
        }
    }

    private boolean checkTokenUrl(@Nonnull final HttpServletRequest request) {
        if (!CollectionUtils.isEmpty(this.whiteMatchers)) {
            for (RequestMatcher m : this.whiteMatchers) {
                if (m != null && m.matches(request)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void doFilterInternal(@Nonnull final HttpServletRequest request, @Nonnull final HttpServletResponse response, @Nonnull final FilterChain chain) throws ServletException, IOException {
        //检查令牌URL是否需要获取令牌
        if (checkTokenUrl(request)) {
            final RequestPath requestPath = RequestPath.parse(request.getRequestURI(), request.getContextPath());
            //获取令牌数据
            final String authorization = request.getHeader(TOKEN_HEADER_NAME);
            //解析令牌
            final Authentication authen = parseToken(requestPath, authorization);
            //将Authentication存入ThreadLocal,方便后续获取用户信息
            SecurityContextHolder.getContext().setAuthentication(authen);
        }
        //链路调用
        chain.doFilter(request, response);
    }

    @Nonnull
    protected Authentication parseToken(@Nonnull final RequestPath path, @Nullable final String authorization) {
        log.debug("parseToken(authorization: {})...", authorization);
        if (Strings.isNullOrEmpty(authorization)) {
            throw new TokenException("令牌为空");
        }
        //解析令牌
        final Ticket ticket = manager.getTokenGenerator().parseToken(authorization);
        final TokenUserDetails userDetails = new TokenUserDetails(ticket);
        //转换用户数据
        return new TokenAuthentication(path, userDetails, null, userDetails.getAuthorities());
    }
}
