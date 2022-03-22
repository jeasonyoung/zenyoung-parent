package top.zenyoung.framework.runtime.aspectj;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.util.ClassUtils;
import top.zenyoung.framework.annotation.PrivacyPolicy;
import top.zenyoung.framework.common.PrivacyPolicyType;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 隐私保护-切面实现
 *
 * @author young
 */
@Slf4j
@Aspect
@Component
public class PrivacyPolicyAspect extends BaseAspect {

    @AfterReturning(pointcut = "@annotation(policy)", returning = "jsonResult")
    public void doAfterReturning(final JoinPoint joinPoint, final PrivacyPolicy policy, final Object jsonResult) {
        if (policy != null && jsonResult != null) {
            buildPrivacyPolicy(jsonResult.getClass(), policy, jsonResult);
        }
    }

    @SneakyThrows
    private void buildPrivacyPolicy(@Nonnull final Class<?> cls, @Nonnull final PrivacyPolicy policy, @Nonnull final Object objVal) {
        final List<Field> fields = Lists.newLinkedList();
        ClassUtils.getAllFieldsWithSuper(cls, fields);
        if (!CollectionUtils.isEmpty(fields)) {
            final List<String> privacyNames = policy.fields() == null ? Lists.newArrayList() : Lists.newArrayList(policy.fields());
            for (Field field : fields) {
                final Class<?> fCls = field.getType();
                final Object fVal = field.get(objVal);
                if (fVal == null) {
                    continue;
                }
                if (field.isAnnotationPresent(PrivacyPolicy.class) || privacyNames.contains(field.getName())) {
                    //设置可见性
                    field.setAccessible(true);
                    //隐私类型
                    final PrivacyPolicy privacyPolicy = field.isAnnotationPresent(PrivacyPolicy.class) ? field.getAnnotation(PrivacyPolicy.class) : policy;
                    //数组处理
                    if (fCls.isArray()) {
                        final Class<?> ec = fVal.getClass().getComponentType();
                        final int len = Array.getLength(fVal);
                        if (ec != null && len > 0) {
                            for (int i = 0; i < len; i++) {
                                final Object o = Array.get(fVal, i);
                                if (o != null) {
                                    final Object n = buildPrivacyHandler(ec, privacyPolicy, o);
                                    if (n != null && !o.equals(n)) {
                                        Array.set(fVal, i, n);
                                    }
                                }
                            }
                        }
                        return;
                    }
                    //集合
                    if (Collection.class.isAssignableFrom(fCls)) {
                        ((Collection<?>) fVal).stream().filter(Objects::nonNull).forEach(v -> {
                            final Object n = buildPrivacyHandler(v.getClass(), privacyPolicy, v);
                            if (n != null && !v.equals(n)) {
                                try {
                                    //移除旧值处理
                                    fCls.getMethod("remove", Object.class).invoke(fVal, v);
                                    //添加新值处理
                                    fCls.getMethod("add", Object.class).invoke(fVal, n);
                                } catch (Throwable ex) {
                                    log.warn("buildPrivacyPolicy(cls: {},privacyPolicy: {})[{}=> {}]-exp: {}", cls, privacyPolicy, v, n, ex.getMessage());
                                }
                            }
                        });
                        return;
                    }
                    //值类型/引用类型处理
                    final String newVal = buildPrivacyHandler(fVal.getClass(), policy, fVal);
                    if (!Strings.isNullOrEmpty(newVal)) {
                        field.set(objVal, newVal);
                    }
                } else if (!isPrimitive(fCls)) {
                    final Object val = field.get(objVal);
                    if (val != null) {
                        buildPrivacyHandler(fCls, policy, val);
                    }
                }
            }
        }
    }

    private String buildPrivacyHandler(@Nonnull final Class<?> cls, @Nonnull final PrivacyPolicy policy, @Nonnull final Object objVal) {
        //值类型处理
        if (isPrimitive(cls)) {
            final String strVal = objVal.toString();
            if (!Strings.isNullOrEmpty(strVal)) {
                //获取策略类型
                final PrivacyPolicyType policyType = policy.policy();
                if (policyType != null) {
                    //脱敏处理
                    return policyType.getPrivacy(strVal);
                }
            }
            return null;
        }
        //引用类型处理
        buildPrivacyPolicy(cls, policy, objVal);
        return null;
    }
}
