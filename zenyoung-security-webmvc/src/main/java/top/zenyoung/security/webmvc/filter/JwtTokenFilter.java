package top.zenyoung.security.webmvc.filter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import top.zenyoung.security.model.LoginReqBody;
import top.zenyoung.security.webmvc.JwtAuthenticationManager;

import javax.annotation.Nonnull;
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
    private final List<RequestMatcher> whiteMatchers = Lists.newLinkedList();
    private final JwtAuthenticationManager<? extends LoginReqBody> manager;

    public JwtTokenFilter(@Nonnull final JwtAuthenticationManager<? extends LoginReqBody> manager) {
        this.manager = manager;
        this.buildWhiteUrls(whiteMatchers, manager);
    }

    private void buildWhiteUrls(@Nonnull final List<RequestMatcher> whiteMatchers, @Nonnull final JwtAuthenticationManager<? extends LoginReqBody> authenticationManager) {
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
        try {
            //检查令牌URL是否需要获取令牌
            if (checkTokenUrl(request)) {
                //解析令牌
                final Authentication authen = manager.parseAuthenticationToken(request);
                //将Authentication存入ThreadLocal,方便后续获取用户信息
                SecurityContextHolder.getContext().setAuthentication(authen);
            }
            //链路调用
            chain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            log.warn("doFilterInternal-exp: {}", ex.getMessage());
            manager.unsuccessfulAuthentication(response, ex);
        }
    }
}
