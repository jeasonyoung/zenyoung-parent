package top.zenyoung.boot.util;

import com.google.common.collect.Sets;
import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * 指定包自动注入工具类
 *
 * @author young
 */
@UtilityClass
public class RegistrarUtils {

    /**
     * 注册Bean定义
     *
     * @param metadata metadata
     * @param registry registry
     * @param annClazz annClazz
     */
    public void registerBeanDefinitions(final AnnotationMetadata metadata, final BeanDefinitionRegistry registry, final Class<?> annClazz) {
        final ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false);
        final Set<String> packagesToScan = getPackagesToScan(annClazz, metadata);
        if (!CollectionUtils.isEmpty(packagesToScan)) {
            scanner.scan(packagesToScan.toArray(new String[]{}));
        }
    }

    private Set<String> getPackagesToScan(final Class<?> annClazz, final AnnotationMetadata metadata) {
        final AnnotationAttributes attributes = getResourceAttributes(annClazz, metadata);
        if (Objects.isNull(attributes)) {
            return Sets.newHashSet();
        }
        final String[] basePackages = attributes.getStringArray("scanBasePackages");
        final Class<?>[] basePackageClasses = attributes.getClassArray("scanBasePackageClasses");
        final Set<String> packagesToScan = Sets.newLinkedHashSet(Arrays.asList(basePackages));
        for (Class<?> clz : basePackageClasses) {
            packagesToScan.add(ClassUtils.getPackageName(clz));
        }
        if (packagesToScan.isEmpty()) {
            packagesToScan.add("top.zenyoung");
        }
        return packagesToScan;
    }

    @Nullable
    private AnnotationAttributes getResourceAttributes(final Class<?> annClazz, AnnotationMetadata metadata) {
        return AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(annClazz.getName()));
    }
}
