package top.zenyoung.boot.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import top.zenyoung.boot.annotation.authority.*;
import top.zenyoung.boot.aop.BaseRequestAuthorityAspect;
import top.zenyoung.boot.util.SecurityUtils;
import top.zenyoung.common.model.UserPrincipal;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * 请求授权访问-拦截器
 *
 * @author young
 */
@Slf4j
public class RequestAuthorityInterceptor extends BaseRequestAuthorityAspect implements RequestMappingInterceptor {
    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public boolean handler(@Nonnull final HttpServletRequest req, @Nonnull final HttpServletResponse res, @Nonnull final HandlerMethod handler) {
        //匿名访问处理
        if (handler.hasMethodAnnotation(HasAnonymous.class)) {
            return true;
        }
        //当前用户信息
        final UserPrincipal principal = SecurityUtils.getPrincipal();
        if (Objects.nonNull(principal)) {
            //拥有某个权限
            final HasPermi hasPermi = handler.getMethodAnnotation(HasPermi.class);
            if (Objects.nonNull(hasPermi)) {
                final String[] hasPermiVal = {hasPermi.value()};
                checkAuthorityHandler(principal, CheckType.PERMI, CheckMethod.IN, hasPermiVal);
                return true;
            }
            //不拥有某权限(与HasPermi逻辑相反)
            final HasLacksPermi hasLacksPermi = handler.getMethodAnnotation(HasLacksPermi.class);
            if (Objects.nonNull(hasLacksPermi)) {
                final String[] hasLacksPermiVal = {hasLacksPermi.value()};
                checkAuthorityHandler(principal, CheckType.PERMI, CheckMethod.NOT_IN, hasLacksPermiVal);
                return true;
            }
            //拥有任意一个权限
            final HasAnyPermi hasAnyPermi = handler.getMethodAnnotation(HasAnyPermi.class);
            if (Objects.nonNull(hasAnyPermi)) {
                final String[] hasAnyPermiVal = hasAnyPermi.value();
                checkAuthorityHandler(principal, CheckType.PERMI, CheckMethod.ANY, hasAnyPermiVal);
                return true;
            }
            //拥有某个角色
            final HasRole hasRole = handler.getMethodAnnotation(HasRole.class);
            if (Objects.nonNull(hasRole)) {
                final String[] hasRoleVal = {hasRole.value()};
                checkAuthorityHandler(principal, CheckType.ROLE, CheckMethod.IN, hasRoleVal);
                return true;
            }
            //不拥有某角色(与HasRole逻辑相反)
            final HasLacksRole hasLacksRole = handler.getMethodAnnotation(HasLacksRole.class);
            if (Objects.nonNull(hasLacksRole)) {
                final String[] hasLacksRoleVal = {hasLacksRole.value()};
                checkAuthorityHandler(principal, CheckType.ROLE, CheckMethod.NOT_IN, hasLacksRoleVal);
                return true;
            }
            //拥有任意一个角色
            final HasAnyRole hasAnyRole = handler.getMethodAnnotation(HasAnyRole.class);
            if (Objects.nonNull(hasAnyRole)) {
                final String[] hasAnyRoleVal = hasAnyRole.value();
                checkAuthorityHandler(principal, CheckType.ROLE, CheckMethod.ANY, hasAnyRoleVal);
                return true;
            }
        }
        return true;
    }
}
