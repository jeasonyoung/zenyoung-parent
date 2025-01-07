package top.zenyoung.boot.aop;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import top.zenyoung.boot.enums.ExceptionEnums;
import top.zenyoung.common.exception.ServiceException;
import top.zenyoung.common.model.UserPrincipal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 请求授权-基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseRequestAuthorityAspect extends BaseAspect {
    protected static final String ALL_PERMISSION = "*:*:*";

    protected void checkAuthorityHandler(@Nullable final UserPrincipal principal, @Nonnull final CheckType type,
                                         @Nonnull final CheckMethod method, @Nullable final String[] vals) throws ServiceException {
        if (Objects.isNull(principal)) {
            throw new ServiceException(ExceptionEnums.UNAUTHORIZED);
        }
        final Collection<String> authorizes = type == CheckType.ROLE ? principal.getRoles() : principal.getPermissions();
        if (CollectionUtils.isEmpty(authorizes) || vals == null || vals.length == 0) {
            log.warn("checkAuthorityHandler[{},{}](authorizes: {}) => {}", type, method, authorizes, vals);
            throw new ServiceException(ExceptionEnums.FORBIDDEN);
        }
        //拥有某个角色/权限
        if (method == CheckMethod.IN && !authorizes.contains(vals[0])) {
            //检查默认全部权限
            if (type == CheckType.PERMI && authorizes.contains(ALL_PERMISSION)) {
                return;
            }
            log.warn("checkAuthorityHandler[{},{}](authorizes: {}) => {}", type, method, authorizes, vals);
            throw new ServiceException(ExceptionEnums.FORBIDDEN);
        }
        //不拥有某个角色/权限
        if (method == CheckMethod.NOT_IN && authorizes.contains(vals[0])) {
            log.warn("checkAuthorityHandler[{},{}](authorizes: {}) => {}", type, method, authorizes, vals);
            throw new ServiceException(ExceptionEnums.FORBIDDEN);
        }
        //拥有任意一个角色/权限
        if (method == CheckMethod.ANY && Stream.of(vals).filter(val -> !Strings.isNullOrEmpty(val)).noneMatch(authorizes::contains)) {
            log.warn("checkAuthorityHandler[{},{}](authorizes: {}) => {}", type, method, authorizes, vals);
            throw new ServiceException(ExceptionEnums.FORBIDDEN);
        }
    }

    protected enum CheckType {
        ROLE,
        PERMI;
    }

    protected enum CheckMethod {
        IN,
        NOT_IN,
        ANY
    }
}
