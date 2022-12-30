package top.zenyoung.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import top.zenyoung.boot.util.RespJsonUtils;
import top.zenyoung.security.service.AuthenManagerService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Jwt令牌认证-过滤器
 *
 * @author young
 */
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {
    private final List<RequestMatcher> whiteMatchers = Lists.newLinkedList();
    private final AuthenManagerService manager;
    private final ObjectMapper objMapper;

    public JwtTokenFilter(@Nullable final AuthenManagerService manager, @Nonnull final ObjectMapper objectMapper) {
        this.manager = manager;
        this.objMapper = objectMapper;
        this.buildWhiteUrls(whiteMatchers);
    }

    private void buildWhiteUrls(@Nonnull final List<RequestMatcher> whiteMatchers) {
        final List<String> whiteUrls = Lists.newLinkedList();
        if (Objects.nonNull(this.manager)) {
            //用户登录
            final String[] loginUrls;
            if (ArrayUtils.isNotEmpty(loginUrls = this.manager.getLoginUrls())) {
                whiteUrls.addAll(Stream.of(loginUrls)
                        .filter(val -> !Strings.isNullOrEmpty(val))
                        .collect(Collectors.toList())
                );
            }
            //白名单
            final String[] urls;
            if (ArrayUtils.isNotEmpty(urls = this.manager.getWhiteUrls())) {
                whiteUrls.addAll(Stream.of(urls)
                        .filter(url -> !Strings.isNullOrEmpty(url))
                        .collect(Collectors.toList())
                );
            }
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
                final Authentication authen = manager.parseAuthenToken(request);
                if (Objects.nonNull(authen)) {
                    //将Authentication存入ThreadLocal,方便后续获取用户信息
                    SecurityContextHolder.getContext().setAuthentication(authen);
                }
            }
        } catch (AuthenticationException ex) {
            RespJsonUtils.buildFailResp(objMapper, response, HttpStatus.UNAUTHORIZED, ex);
            log.warn("doFilterInternal-exp: {}", ex.getMessage());
            return;
        } catch (Throwable ex) {
            RespJsonUtils.buildFailResp(objMapper, response, HttpStatus.BAD_REQUEST, ex);
            log.warn("doFilterInternal-exp: {}", ex.getMessage());
            return;
        }
        //链路调用
        chain.doFilter(request, response);
    }
}
