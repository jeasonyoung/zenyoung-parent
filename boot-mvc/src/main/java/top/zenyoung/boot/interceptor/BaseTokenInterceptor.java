package top.zenyoung.boot.interceptor;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import top.zenyoung.boot.annotation.HasAnonymous;
import top.zenyoung.boot.enums.ExceptionEnums;
import top.zenyoung.common.exception.ServiceException;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.common.util.SecurityUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

    @Override
    public int getOrder() {
        return -Integer.MAX_VALUE;
    }

    @Override
    public final boolean handler(@Nonnull final HttpServletRequest req, @Nonnull final HttpServletResponse res, @Nonnull final HandlerMethod handler) {
        //获取令牌
        final String token = Optional.ofNullable(req.getHeader(TOKEN_NAME))
                .filter(val -> !Strings.isNullOrEmpty(val))
                .orElseGet(() -> req.getParameter(TOKEN_NAME));
        if (Strings.isNullOrEmpty(token)) {
            //检查是允许匿名访问
            if (handler.hasMethodAnnotation(HasAnonymous.class)) {
                return true;
            }
            log.warn("获取令牌为空=> {}", req.getRequestURI());
            throw new ServiceException(ExceptionEnums.UNAUTHORIZED);
        }
        //检查令牌
        final String tokenVal = token.startsWith(AUTH_BEARER_PREFIX) ? StringUtils.replace(token, AUTH_BEARER_PREFIX, "").trim() : token;
        //解析令牌数据
        log.info("parseAccessToken: {} => {}", req.getRequestURI(), tokenVal);
        final UserPrincipal principal = parseAccessToken(tokenVal, handler);
        if (Objects.nonNull(principal)) {
            SecurityUtils.setPrincipal(principal);
        }
        return true;
    }

    /**
     * 解析令牌
     *
     * @param accessToken   访问令牌
     * @param handlerMethod 处理方法接口
     * @return 用户信息
     */
    protected abstract UserPrincipal parseAccessToken(@Nonnull final String accessToken, @Nonnull final HandlerMethod handlerMethod);

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
