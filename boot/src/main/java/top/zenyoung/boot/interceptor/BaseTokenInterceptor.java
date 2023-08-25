package top.zenyoung.boot.interceptor;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import top.zenyoung.boot.annotation.authorize.HasAnonymous;
import top.zenyoung.boot.enums.ExceptionEnums;
import top.zenyoung.boot.util.SecurityUtils;
import top.zenyoung.common.exception.ServiceException;
import top.zenyoung.common.model.UserPrincipal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 令牌处理-拦截器
 *
 * @author young
 */
@Slf4j
public abstract class BaseTokenInterceptor implements RequestMappingInterceptor {
    private static final String TOKEN_NAME = HttpHeaders.AUTHORIZATION;
    private static final String AUTH_BEARER_PREFIX = "Bearer ";
    protected static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    protected static final Cache<Long, UserPrincipal> USER_PRINCIPAL_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(300))
            .build();

    @Override
    public int getOrder() {
        return -Integer.MAX_VALUE;
    }

    @Override
    public final boolean handler(@Nonnull final HttpServletRequest req, @Nonnull final HttpServletResponse res,
                                 @Nonnull final HandlerMethod handler) {
        //获取匿名登录注解
        final HasAnonymous pt = handler.getMethodAnnotation(HasAnonymous.class);
        //是否非匿名访问
        final boolean notAnonymous = Objects.isNull(pt);
        //获取令牌
        String token = Optional.ofNullable(req.getHeader(TOKEN_NAME))
                .filter(val -> !Strings.isNullOrEmpty(val))
                .orElse(req.getParameter(TOKEN_NAME));
        if (Strings.isNullOrEmpty(token) && notAnonymous) {
            log.warn("获取令牌为空=> {}", req.getRequestURI());
            throw new ServiceException(ExceptionEnums.UNAUTHORIZED);
        }
        //解析访问令牌
        if (!Strings.isNullOrEmpty(token)) {
            //检查令牌
            if (token.startsWith(AUTH_BEARER_PREFIX)) {
                token = StringUtils.replace(token, AUTH_BEARER_PREFIX, "").trim();
            }
            //解析令牌数据
            final UserPrincipal principal = parseAccessToken(token, notAnonymous);
            if (Objects.nonNull(principal)) {
                SecurityUtils.setPrincipal(principal);
            }
        }
        return true;
    }

    /**
     * 解析令牌
     *
     * @param accessToken  访问令牌
     * @param notAnonymous 是否非匿名访问
     * @return 用户信息
     */
    protected abstract UserPrincipal parseAccessToken(@Nonnull final String accessToken, final boolean notAnonymous);

    /**
     * 数据转换处理
     *
     * @param items   源数据集合
     * @param handler 转换处理
     * @param <T>     结果数据类型
     * @return 结果数据集合
     */
    protected static <T> Set<String> dataConvertHandler(@Nullable final List<T> items, @Nonnull final Function<T, String> handler) {
        if (!CollectionUtils.isEmpty(items)) {
            return items.stream()
                    .filter(Objects::nonNull)
                    .map(handler)
                    .filter(val -> !Strings.isNullOrEmpty(val))
                    .collect(Collectors.toSet());
        }
        return Sets.newHashSet();
    }
}
