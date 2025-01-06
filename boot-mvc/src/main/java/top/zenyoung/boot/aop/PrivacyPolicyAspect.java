package top.zenyoung.boot.aop;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import top.zenyoung.boot.annotation.PrivacyPolicy;
import top.zenyoung.boot.annotation.PrivacyPolicyType;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.vo.ResultVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 隐私保护-切面实现
 *
 * @author young
 */
@Slf4j
@Aspect
public class PrivacyPolicyAspect extends BaseAspect {

    @AfterReturning(pointcut = "@annotation(policy)", returning = "ret")
    public void doAfterReturning(final JoinPoint joinPoint, final PrivacyPolicy policy, final Object ret) {
        if (Objects.isNull(ret)) {
            return;
        }
        //检查是否为响应结果
        if (ret instanceof ResultVO<?> vo) {
            final Object data = vo.getData();
            if (Objects.isNull(data)) {
                log.warn("PrivacyPolicy aop 拦截结果数据为null => {}", ret);
                return;
            }
            //检查是否为分页
            if (data instanceof PageList<?> pageList) {
                final Collection<?> rows = pageList.getRows();
                if (!CollectionUtils.isEmpty(rows)) {
                    rows.forEach(row -> buildPrivacyPolicy(row, policy));
                }
                return;
            }
            //检查是否为集合
            final Class<?> dataCls = data.getClass();
            if (Collection.class.isAssignableFrom(dataCls)) {
                ((Collection<?>) data).forEach(row -> buildPrivacyPolicy(row, policy));
                return;
            }
            //是否为值类型
            if (ClassUtils.isPrimitiveOrWrapper(dataCls) || dataCls == String.class) {
                log.warn("PrivacyPolicy aop 拦截结果数据为值类型=> {}", dataCls);
                return;
            }
            //引用类型处理
            buildPrivacyPolicy(data, policy);
        }
    }

    private void buildPrivacyPolicy(@Nullable final Object data, @Nonnull final PrivacyPolicy policy) {
        if (Objects.isNull(data)) {
            log.warn("buildPrivacyPolicy: 数据为null");
            return;
        }
        final Function<String[], Set<String>> policyHandler = policyVals -> {
            if (policyVals != null && policyVals.length > 0) {
                return Stream.of(policyVals)
                        .filter(val -> !Strings.isNullOrEmpty(val))
                        .collect(Collectors.toSet());
            }
            return Sets.newHashSet();
        };
        final Set<String> policyMobiles = policyHandler.apply(policy.mobiles()), policyIdCards = policyHandler.apply(policy.idCards());
        ReflectionUtils.doWithFields(data.getClass(), field -> {
            final Object val = ReflectionUtils.getField(field, data);
            if (Objects.nonNull(val)) {
                //检查是否有注解
                final PrivacyPolicy privacyPolicy = AnnotatedElementUtils.findMergedAnnotation(field, PrivacyPolicy.class);
                //检查是否为值类型
                final Class<?> cls = field.getDeclaringClass();
                if (!ClassUtils.isPrimitiveOrWrapper(cls) && cls != String.class) {
                    buildPrivacyPolicy(val, Objects.nonNull(privacyPolicy) ? privacyPolicy : policy);
                } else if (val instanceof String privacyVal) {
                    final String fieldName = field.getName();
                    if (Objects.nonNull(privacyPolicy) && Objects.nonNull(privacyPolicy.policy())
                            && privacyPolicy.policy() != PrivacyPolicyType.NULL) {
                        //注解脱敏
                        buildPrivacyHandler(data, privacyVal, field, privacyPolicy.policy());
                    } else if (!CollectionUtils.isEmpty(policyMobiles) && policyMobiles.contains(fieldName)) {
                        //手机号码脱敏
                        buildPrivacyHandler(data, privacyVal, field, PrivacyPolicyType.MOBILE);
                    } else if (!CollectionUtils.isEmpty(policyIdCards) && policyIdCards.contains(fieldName)) {
                        //身份证号码脱敏
                        buildPrivacyHandler(data, privacyVal, field, PrivacyPolicyType.ID_CARD);
                    }
                }
            }
        }, field -> {
            final String fieldName = field.getName();
            if (!CollectionUtils.isEmpty(policyMobiles) && policyMobiles.contains(fieldName)) {
                return true;
            }
            if (!CollectionUtils.isEmpty(policyIdCards) && policyIdCards.contains(fieldName)) {
                return true;
            }
            return AnnotatedElementUtils.hasAnnotation(field, PrivacyPolicy.class);
        });
    }

    private void buildPrivacyHandler(@Nonnull final Object obj, @Nonnull final String val,
                                     @Nonnull final Field field, @Nonnull final PrivacyPolicyType type) {
        try {
            //脱敏数据
            final String retVal = type.getPrivacy(val);
            if (!Strings.isNullOrEmpty(retVal)) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, obj, retVal);
            }
        } catch (Exception e) {
            log.warn("buildPrivacyHandler(val: {})-exp: {}", val, e.getMessage());
        }
    }
}
