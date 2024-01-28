package top.zenyoung.boot.filter;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import top.zenyoung.boot.annotation.authority.*;
import top.zenyoung.boot.constant.HeaderConstants;
import top.zenyoung.boot.enums.ExceptionEnums;
import top.zenyoung.common.exception.ServiceException;
import top.zenyoung.common.model.UserPrincipal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 授权访问-过滤器
 *
 * @author young
 */
@Slf4j
public abstract class BaseAuthorizeFilter extends BaseWebFilter {
    protected static final String ALL_PERMISSION = "*:*:*";

    protected BaseAuthorizeFilter(@Nonnull final RequestMappingHandlerMapping handlerMapping) {
        super(handlerMapping);
    }

    @Nonnull
    @Override
    protected final Mono<Void> handler(@Nonnull final ServerWebExchange exchange,
                                       @Nonnull final WebFilterChain chain,
                                       @Nonnull final HandlerMethod method) {
        final String accountId = exchange.getRequest()
                .getHeaders()
                .getFirst(HeaderConstants.ACCOUNT_ID);
        //拥有某个权限
        final HasPermi hasPermi = method.getMethodAnnotation(HasPermi.class);
        if (Objects.nonNull(hasPermi)) {
            final String[] hasPermiVal = {hasPermi.value()};
            return doAuthorizeHandler(accountId, CheckType.PERMI, CheckMethod.IN, hasPermiVal, exchange, chain);
        }
        //不拥有某权限(与HasPermi逻辑相反)
        final HasLacksPermi hasLacksPermi = method.getMethodAnnotation(HasLacksPermi.class);
        if (Objects.nonNull(hasLacksPermi)) {
            final String[] hasLacksPermiVal = {hasLacksPermi.value()};
            return doAuthorizeHandler(accountId, CheckType.PERMI, CheckMethod.NOT_IN, hasLacksPermiVal, exchange, chain);
        }
        //拥有任意一个权限
        final HasAnyPermi hasAnyPermi = method.getMethodAnnotation(HasAnyPermi.class);
        if (Objects.nonNull(hasAnyPermi)) {
            final String[] hasAnyPermiVal = hasAnyPermi.value();
            return doAuthorizeHandler(accountId, CheckType.PERMI, CheckMethod.ANY, hasAnyPermiVal, exchange, chain);
        }
        //拥有某个角色
        final HasRole hasRole = method.getMethodAnnotation(HasRole.class);
        if (Objects.nonNull(hasRole)) {
            final String[] hasRoleVal = {hasRole.value()};
            return doAuthorizeHandler(accountId, CheckType.ROLE, CheckMethod.IN, hasRoleVal, exchange, chain);
        }
        //不拥有某角色(与HasRole逻辑相反)
        final HasLacksRole hasLacksRole = method.getMethodAnnotation(HasLacksRole.class);
        if (Objects.nonNull(hasLacksRole)) {
            final String[] hasLacksRoleVal = {hasLacksRole.value()};
            return doAuthorizeHandler(accountId, CheckType.ROLE, CheckMethod.NOT_IN, hasLacksRoleVal, exchange, chain);
        }
        //拥有任意一个角色
        final HasAnyRole hasAnyRole = method.getMethodAnnotation(HasAnyRole.class);
        if (Objects.nonNull(hasAnyRole)) {
            final String[] hasAnyRoleVal = hasAnyRole.value();
            return doAuthorizeHandler(accountId, CheckType.ROLE, CheckMethod.ANY, hasAnyRoleVal, exchange, chain);
        }
        //没有权限处理
        return chain.filter(exchange);
    }

    private Mono<Void> doAuthorizeHandler(@Nullable final String accountId, @Nonnull final CheckType type,
                                          @Nonnull final CheckMethod method, @Nullable final String[] vals,
                                          @Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain) {
        if (Strings.isNullOrEmpty(accountId)) {
            log.warn("'accountId'为空.");
            return Mono.error(new ServiceException(ExceptionEnums.UNAUTHORIZED));
        }
        return buildPrincipal(accountId)
                .switchIfEmpty(Mono.error(new ServiceException(ExceptionEnums.UNAUTHORIZED)))
                .flatMap(principal -> checkAuthorityHandler(principal, type, method, vals)
                        .flatMap(ret -> {
                            if (ret) {
                                return chain.filter(exchange);
                            }
                            log.warn("doAuthorizeHandler(accountId: {},type: {},method: {})=> {}", accountId, type, method, vals);
                            return Mono.error(new ServiceException(ExceptionEnums.FORBIDDEN));
                        })
                );
    }


    /**
     * 根据账号ID加载用户信息
     *
     * @param accountId 账号ID
     * @return 用户信息
     */
    @Nonnull
    protected abstract Mono<UserPrincipal> buildPrincipal(@Nonnull final String accountId);

    /**
     * 检查授权处理
     *
     * @param principal 用户信息
     * @param type      检查类型
     * @param method    检查方法
     * @param vals      检查值
     * @return 检查结果
     */
    private Mono<Boolean> checkAuthorityHandler(@Nullable final UserPrincipal principal,
                                                @Nonnull final CheckType type,
                                                @Nonnull final CheckMethod method,
                                                @Nullable final String[] vals) {
        if (Objects.isNull(principal)) {
            return Mono.error(new ServiceException(ExceptionEnums.UNAUTHORIZED));
        }
        final Mono<Boolean> success = Mono.just(true), forbidden = Mono.error(new ServiceException(ExceptionEnums.FORBIDDEN));
        final Set<String> authorizes = type == CheckType.ROLE ? principal.getRoles() : principal.getPermissions();
        if (CollectionUtils.isEmpty(authorizes) || vals == null || vals.length == 0) {
            log.warn("checkAuthorityHandler[{},{}](authorizes: {}) => {}", type, method, authorizes, vals);
            return forbidden;
        }
        //拥有某个角色/权限
        if (method == CheckMethod.IN && !authorizes.contains(vals[0])) {
            //检查默认全部权限
            if (type == CheckType.PERMI && authorizes.contains(ALL_PERMISSION)) {
                return success;
            }
            log.warn("checkAuthorityHandler[{},{}](authorizes: {}) => {}", type, method, authorizes, vals);
            return forbidden;
        }
        //不拥有某个角色/权限
        if (method == CheckMethod.NOT_IN && authorizes.contains(vals[0])) {
            log.warn("checkAuthorityHandler[{},{}](authorizes: {}) => {}", type, method, authorizes, vals);
            return forbidden;
        }
        //拥有任意一个角色/权限
        if (method == CheckMethod.ANY && Stream.of(vals).filter(val -> !Strings.isNullOrEmpty(val)).noneMatch(authorizes::contains)) {
            log.warn("checkAuthorityHandler[{},{}](authorizes: {}) => {}", type, method, authorizes, vals);
            return forbidden;
        }
        return success;
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
