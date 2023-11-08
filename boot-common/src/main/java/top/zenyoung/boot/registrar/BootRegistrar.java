package top.zenyoung.boot.registrar;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import top.zenyoung.boot.annotation.Boot;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

/**
 * 指定包注解类自动注入
 *
 * @author young
 */
@Slf4j
public class BootRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(@Nonnull final AnnotationMetadata metadata, @Nonnull final BeanDefinitionRegistry registry) {
        final ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false);
        final Set<String> packagesToScan = getPackagesToScan(metadata);
        scanner.scan(packagesToScan.toArray(new String[]{}));
    }

    private static Set<String> getPackagesToScan(@Nonnull final AnnotationMetadata metadata) {
        final Set<String> packagesToScan = Sets.newLinkedHashSet();
        final AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(Boot.class.getName()));
        if (Objects.nonNull(attributes)) {
            final String[] basePackages = attributes.getStringArray("scanBasePackages");
            if (basePackages.length > 0) {
                packagesToScan.addAll(Lists.newArrayList(basePackages));
            }
            final Class<?>[] basePackageClasses = attributes.getClassArray("scanBasePackageClasses");
            for (final Class<?> cls : basePackageClasses) {
                packagesToScan.add(ClassUtils.getPackageName(cls));
            }
            if (CollectionUtils.isEmpty(packagesToScan)) {
                packagesToScan.add("top.zenyoung");
            }
        }
        return packagesToScan;
    }
}
