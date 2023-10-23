package top.zenyoung.boot.interceptor;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import top.zenyoung.boot.annotation.authorize.*;
import top.zenyoung.boot.enums.ExceptionEnums;
import top.zenyoung.common.exception.ServiceException;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.common.util.SecurityUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * 请求授权访问-拦截器
 *
 * @author young
 */
@Slf4j
public class RequestAuthorizeInterceptor implements RequestMappingInterceptor {
    private static final String ALL_PERMISSION = "*:*:*";

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
            //检查处理
            final BiPredicate<Pair<CheckType, CheckMethod>, String[]> checkAuthorizeHandler = (p, authorize) -> {
                final CheckType type = p.getLeft();
                final CheckMethod method = p.getRight();
                final Set<String> authorizes = type == CheckType.ROLE ? principal.getRoles() : principal.getPermissions();
                if (CollectionUtils.isEmpty(authorizes) || ArrayUtils.isEmpty(authorize) || Strings.isNullOrEmpty(authorize[0])) {
                    log.warn("checkAuthorizeHandler[{}](authorize: {}) => authorizes: {}", type, String.join(",", authorize), authorizes);
                    throw new ServiceException(ExceptionEnums.FORBIDDEN);
                }
                //拥有某个角色/权限
                if (method == CheckMethod.IN && !authorizes.contains(authorize[0])) {
                    //检查默认全部权限
                    if (type == CheckType.PERMI && authorizes.contains(ALL_PERMISSION)) {
                        return true;
                    }
                    log.warn("checkAuthorizeHandler[{}, {}]=> authorize: {}", type, method, authorize[0]);
                    throw new ServiceException(ExceptionEnums.FORBIDDEN);
                }
                //不拥有某个角色/权限
                if (method == CheckMethod.NOT_IN && authorizes.contains(authorize[0])) {
                    log.warn("checkAuthorizeHandler[{}, {}]=> authorize: {}", type, method, authorize[0]);
                    throw new ServiceException(ExceptionEnums.FORBIDDEN);
                }
                //拥有任意一个角色/权限
                if (method == CheckMethod.ANY && Stream.of(authorize).noneMatch(authorizes::contains)) {
                    log.warn("checkAuthorizeHandler[{}, {}]=> authorize: {}", type, method, String.join(",", authorize));
                    throw new ServiceException(ExceptionEnums.FORBIDDEN);
                }
                return true;
            };
            //拥有某个权限
            final HasPermi hasPermi = handler.getMethodAnnotation(HasPermi.class);
            if (Objects.nonNull(hasPermi)) {
                final String[] hasPermiVal = {hasPermi.value()};
                return checkAuthorizeHandler.test(Pair.of(CheckType.PERMI, CheckMethod.IN), hasPermiVal);
            }
            //不拥有某权限(与HasPermi逻辑相反)
            final HasLacksPermi hasLacksPermi = handler.getMethodAnnotation(HasLacksPermi.class);
            if (Objects.nonNull(hasLacksPermi)) {
                final String[] hasLacksPermiVal = {hasLacksPermi.value()};
                return checkAuthorizeHandler.test(Pair.of(CheckType.PERMI, CheckMethod.NOT_IN), hasLacksPermiVal);
            }
            //拥有任意一个权限
            final HasAnyPermi hasAnyPermi = handler.getMethodAnnotation(HasAnyPermi.class);
            if (Objects.nonNull(hasAnyPermi)) {
                final String[] hasAnyPermiVal = hasAnyPermi.value();
                return checkAuthorizeHandler.test(Pair.of(CheckType.PERMI, CheckMethod.ANY), hasAnyPermiVal);
            }
            //拥有某个角色
            final HasRole hasRole = handler.getMethodAnnotation(HasRole.class);
            if (Objects.nonNull(hasRole)) {
                final String[] hasRoleVal = {hasRole.value()};
                return checkAuthorizeHandler.test(Pair.of(CheckType.ROLE, CheckMethod.IN), hasRoleVal);
            }
            //不拥有某角色(与HasRole逻辑相反)
            final HasLacksRole hasLacksRole = handler.getMethodAnnotation(HasLacksRole.class);
            if (Objects.nonNull(hasLacksRole)) {
                final String[] hasLacksRoleVal = {hasLacksRole.value()};
                return checkAuthorizeHandler.test(Pair.of(CheckType.ROLE, CheckMethod.NOT_IN), hasLacksRoleVal);
            }
            //拥有任意一个角色
            final HasAnyRole hasAnyRole = handler.getMethodAnnotation(HasAnyRole.class);
            if (Objects.nonNull(hasAnyRole)) {
                final String[] hasAnyRoleVal = hasAnyRole.value();
                return checkAuthorizeHandler.test(Pair.of(CheckType.ROLE, CheckMethod.ANY), hasAnyRoleVal);
            }
        }
        return true;
    }

    private enum CheckType {
        ROLE,
        PERMI;
    }

    private enum CheckMethod {
        IN,
        NOT_IN,
        ANY
    }
}
