package top.zenyoung.retrofit.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 自动配置扫描与SpringBoot相同路径的包,
 * 如果需要扫描更多的包路径,请使用 {@link top.zenyoung.retrofit.annotation.RetrofitClientScan}
 */
@Slf4j
public class AutoConfiguredRetrofitScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar,
        ResourceLoaderAware, BeanClassLoaderAware {
    private BeanFactory beanFactory;
    private ResourceLoader resourceLoader;
    private ClassLoader classLoader;

    @Override
    public void setBeanFactory(@Nonnull final BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setResourceLoader(@Nonnull final ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setBeanClassLoader(@Nonnull final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void registerBeanDefinitions(@Nonnull final AnnotationMetadata importingClassMetadata, @Nonnull final BeanDefinitionRegistry registry) {
        if(!AutoConfigurationPackages.has(this.beanFactory)){
            log.warn("Could not determine auto-configuration package, automatic retrofit scanning disabled.");
            return;
        }
        final List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
        // Scan the @RetrofitClient annotated interface under the specified path and register it to the BeanDefinitionRegistry
        final ClassPathRetrofitClientScanner scanner = new ClassPathRetrofitClientScanner(registry, classLoader);
        if(Objects.nonNull(resourceLoader)){
            scanner.setResourceLoader(resourceLoader);
        }
        final String[] packageItems = packages.toArray(new String[0]);
        log.info("Scan the @RetrofitClient annotated interface using the auto-configuration package. packages={}",
                Arrays.toString(packageItems));
        scanner.registerFilters();
        // Scan and register to BeanDefinition
        scanner.doScan(packageItems);
    }
}
