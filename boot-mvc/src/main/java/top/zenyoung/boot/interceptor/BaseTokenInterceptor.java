package top.zenyoung.boot.interceptor;

import com.google.common.base.Strings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import top.zenyoung.boot.annotation.authority.HasAnonymous;
import top.zenyoung.boot.enums.ExceptionEnums;
import top.zenyoung.boot.util.HttpUtils;
import top.zenyoung.boot.util.SecurityUtils;
import top.zenyoung.common.exception.ServiceException;
import top.zenyoung.common.model.UserPrincipal;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * 令牌处理-拦截器
 *
 * @author young
 */
@Slf4j
public abstract class BaseTokenInterceptor implements RequestMappingInterceptor {

    @Override
    public int getOrder() {
        return -Integer.MAX_VALUE;
    }

    @Override
    public final boolean handler(@Nonnull final HttpServletRequest req, @Nonnull final HttpServletResponse res, @Nonnull final HandlerMethod handler) {
        //获取令牌
        final String token = HttpUtils.getToken(req);
        if (Strings.isNullOrEmpty(token)) {
            //检查是允许匿名访问
            if (handler.hasMethodAnnotation(HasAnonymous.class)) {
                return true;
            }
            log.warn("获取令牌为空=> {}", req.getRequestURI());
            throw new ServiceException(ExceptionEnums.UNAUTHORIZED);
        }
        //解析令牌数据
        log.info("parseAccessToken: {} => {}", req.getRequestURI(), token);
        final UserPrincipal principal = parseAccessToken(token, handler);
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
}
