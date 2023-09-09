package top.zenyoung.retrofit.core;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import top.zenyoung.retrofit.annotation.RetrofitClientScan;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * Retrofit-客户端扫描注册处理
 *
 * @author young
 */
@Slf4j
public class RetrofitClientScannerRegistrar implements ImportBeanDefinitionRegistrar,
        ResourceLoaderAware, BeanClassLoaderAware {
    private ResourceLoader resourceLoader;
    private ClassLoader classLoader;

    @Override
    public void registerBeanDefinitions(@Nonnull final AnnotationMetadata metadata, @Nonnull final BeanDefinitionRegistry registry) {
        final AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(RetrofitClientScan.class.getName()));
        if (Objects.isNull(attributes)) {
            return;
        }
        // Scan the @RetrofitClient annotated interface under the specified path and register it to the
        // BeanDefinitionRegistry
        final ClassPathRetrofitClientScanner scanner = new ClassPathRetrofitClientScanner(registry, classLoader);
        if (Objects.nonNull(resourceLoader)) {
            scanner.setResourceLoader(resourceLoader);
        }
        // Specify the base package for scanning
        final String[] basePackages = getPackagesToScan(attributes);
        log.info("Scan the @RetrofitClient annotated interface using the @RetrofitClientScan configuration. packages={}",
                Arrays.toString(basePackages));
        scanner.registerFilters();
        //Scan and register to BeanDefinition
        scanner.doScan(basePackages);
    }

    private String[] getPackagesToScan(@Nonnull final AnnotationAttributes attributes) {
        final String[] basePackages = attributes.getStringArray("basePackages");
        final Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
        final Set<String> packagesToScan = Sets.newLinkedHashSet();
        if (!ObjectUtils.isEmpty(basePackages)) {
            packagesToScan.addAll(Arrays.asList(basePackages));
        }
        if (!ObjectUtils.isEmpty(basePackageClasses)) {
            for (final Class<?> basePackageClass : basePackageClasses) {
                packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
            }
        }
        return packagesToScan.toArray(new String[0]);
    }

    @Override
    public void setBeanClassLoader(@Nonnull final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setResourceLoader(@Nonnull final ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
