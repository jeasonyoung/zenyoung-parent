package top.zenyoung.boot.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;
import top.zenyoung.boot.annotation.authority.*;
import top.zenyoung.boot.util.SecurityUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 请求授权访问-aop
 *
 * @author young
 */
@Slf4j
@Order(0)
public class RequestAuthorizeAspect extends BaseRequestAuthorityAspect {

    @Before("@annotation(hasPermi)")
    public Mono<?> doBefore(@Nonnull final JoinPoint joinPoint, @Nonnull final HasPermi hasPermi) {
        final String[] hasPermiVal = {hasPermi.value()};
        return doBeforeHandler(joinPoint, CheckType.PERMI, CheckMethod.IN, hasPermiVal);
    }

    @Before("@annotation(hasLacksPermi)")
    public Mono<?> doBefore(@Nonnull final JoinPoint joinPoint, @Nonnull final HasLacksPermi hasLacksPermi) {
        final String[] hasLacksPermiVal = {hasLacksPermi.value()};
        return doBeforeHandler(joinPoint, CheckType.PERMI, CheckMethod.NOT_IN, hasLacksPermiVal);
    }

    @Before("@annotation(hasAnyPermi)")
    public Mono<?> doBefore(@Nonnull final JoinPoint joinPoint, @Nonnull final HasAnyPermi hasAnyPermi) {
        final String[] hasAnyPermiVal = hasAnyPermi.value();
        return doBeforeHandler(joinPoint, CheckType.PERMI, CheckMethod.ANY, hasAnyPermiVal);
    }

    @Before("@annotation(hasRole)")
    public Mono<?> doBefore(@Nonnull final JoinPoint joinPoint, @Nonnull final HasRole hasRole) {
        final String[] hasRoleVal = {hasRole.value()};
        return doBeforeHandler(joinPoint, CheckType.ROLE, CheckMethod.IN, hasRoleVal);
    }

    @Before("@annotation(hasLacksRole)")
    public Mono<?> doBefore(@Nonnull final JoinPoint joinPoint, @Nonnull final HasLacksRole hasLacksRole) {
        final String[] hasLacksRoleVal = {hasLacksRole.value()};
        return doBeforeHandler(joinPoint, CheckType.ROLE, CheckMethod.NOT_IN, hasLacksRoleVal);
    }

    @Before("@annotation(hasLacksRole)")
    public Mono<?> doBefore(@Nonnull final JoinPoint joinPoint, @Nonnull final HasAnyRole hasAnyRole) {
        final String[] hasAnyRoleVal = hasAnyRole.value();
        return doBeforeHandler(joinPoint, CheckType.ROLE, CheckMethod.ANY, hasAnyRoleVal);
    }

    private Mono<?> doBeforeHandler(@Nonnull final JoinPoint joinPoint, @Nonnull final CheckType type,
                                    @Nonnull final CheckMethod method, @Nullable final String[] vals) {
        return SecurityUtils.getPrincipal()
                .flatMap(principal -> {
                    try {
                        checkAuthorityHandler(principal, type, method, vals);
                        return (Mono<?>) (((ProceedingJoinPoint) joinPoint).proceed());
                    } catch (Throwable e) {
                        return Mono.error(e);
                    }
                });
    }
}
