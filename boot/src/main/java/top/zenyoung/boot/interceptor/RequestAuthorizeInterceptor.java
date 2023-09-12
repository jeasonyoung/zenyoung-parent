package top.zenyoung.boot.interceptor;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import top.zenyoung.boot.annotation.authorize.*;
import top.zenyoung.boot.enums.ExceptionEnums;
import top.zenyoung.common.util.SecurityUtils;
import top.zenyoung.common.exception.ServiceException;
import top.zenyoung.common.model.UserPrincipal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

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
        final HasAnonymous hasAnonymous = handler.getMethodAnnotation(HasAnonymous.class);
        if (Objects.nonNull(hasAnonymous)) {
            log.info("匿名访问");
            return true;
        }
        //当前用户信息
        final UserPrincipal principal = SecurityUtils.getPrincipal();
        if (Objects.nonNull(principal)) {
            final Set<String> permissions = principal.getPermissions();
            final Set<String> roles = principal.getRoles();
            //拥有某个权限
            final HasPermi hasPermi = handler.getMethodAnnotation(HasPermi.class);
            if (checkHandler(permissions, hasPermi, (perms, hpi) -> checkHasAuthorize(perms, hpi.value()))) {
                log.info("拥有权限: {}", hasPermi);
                return true;
            }
            //不拥有某权限(与HasPermi逻辑相反)
            final HasLacksPermi hasLacksPermi = handler.getMethodAnnotation(HasLacksPermi.class);
            if (checkHandler(permissions, hasLacksPermi, (perms, hpi) -> !Strings.isNullOrEmpty(hpi.value()) && !checkHasAuthorize(perms, hpi.value()))) {
                log.info("不拥有权限: {}", hasLacksPermi);
                return true;
            }
            //拥有任意一个权限
            final HasAnyPermi hasAnyPermi = handler.getMethodAnnotation(HasAnyPermi.class);
            if (checkHandler(permissions, hasAnyPermi, (perms, hai) -> checkHasAnyAuthorize(perms, hai.value()))) {
                log.info("拥有任意一个权限: {}", hasAnyPermi);
                return true;
            }
            //拥有某个角色
            final HasRole hasRole = handler.getMethodAnnotation(HasRole.class);
            if (checkHandler(roles, hasRole, (r, hr) -> checkHasAuthorize(r, hr.value()))) {
                log.info("拥有某个角色: {}", hasRole);
                return true;
            }
            //不拥有某角色(与HasRole逻辑相反)
            final HasLacksRole hasLacksRole = handler.getMethodAnnotation(HasLacksRole.class);
            if (checkHandler(roles, hasLacksRole, (r, hlr) -> !Strings.isNullOrEmpty(hlr.value()) && !checkHasAuthorize(r, hlr.value()))) {
                log.info("不拥有角色: {}", hasLacksRole);
                return true;
            }
            //拥有任意一个角色
            final HasAnyRole hasAnyRole = handler.getMethodAnnotation(HasAnyRole.class);
            if (checkHandler(roles, hasAnyRole, (r, har) -> checkHasAnyAuthorize(r, har.value()))) {
                log.info("拥有任意一个角色: {}", hasAnyRole);
                return true;
            }
        }
        throw new ServiceException(ExceptionEnums.FORBIDDEN);
    }

    private <T> boolean checkHandler(@Nullable final Set<String> sources, @Nullable final T ann,
                                     @Nonnull final BiPredicate<Set<String>, T> handler) {
        if (CollectionUtils.isEmpty(sources) || Objects.isNull(ann)) {
            return false;
        }
        return handler.test(sources, ann);
    }

    private boolean checkHasAuthorize(@Nonnull final Set<String> authorizes, @Nullable final String authorize) {
        if (authorizes.contains(ALL_PERMISSION)) {
            return true;
        }
        if (Strings.isNullOrEmpty(authorize)) {
            return false;
        }
        return authorizes.contains(StringUtils.trim(authorize));
    }

    private boolean checkHasAnyAuthorize(@Nonnull final Set<String> authorizes, @Nullable final String[] anyAuthorizes) {
        if (!CollectionUtils.isEmpty(authorizes) && Objects.nonNull(anyAuthorizes)) {
            for (final String authorize : anyAuthorizes) {
                if (checkHasAuthorize(authorizes, authorize)) {
                    return true;
                }
            }
        }
        return false;
    }
}
