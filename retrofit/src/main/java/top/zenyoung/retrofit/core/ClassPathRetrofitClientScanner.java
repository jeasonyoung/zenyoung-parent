package top.zenyoung.retrofit.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import top.zenyoung.retrofit.annotation.RetrofitClient;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * RetrofitClient Scanner
 *
 * @author young
 */
@Slf4j
public class ClassPathRetrofitClientScanner extends ClassPathBeanDefinitionScanner {
    private final ClassLoader classLoader;

    public ClassPathRetrofitClientScanner(@Nonnull final BeanDefinitionRegistry registry, @Nonnull final ClassLoader classLoader) {
        super(registry, false);
        this.classLoader = classLoader;
    }

    public void registerFilters() {
        this.addIncludeFilter(new AnnotationTypeFilter(RetrofitClient.class));
    }

    @Nonnull
    @Override
    protected Set<BeanDefinitionHolder> doScan(@Nonnull final String... basePackages) {
        final Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        if (!CollectionUtils.isEmpty(beanDefinitions)) {
            processBeanDefinitions(beanDefinitions);
        } else {
            final String basePackagesVals = Arrays.toString(basePackages);
            log.warn("No RetrofitClient was found in '{}' package. Please check your configuration.", basePackagesVals);
        }
        return beanDefinitions;
    }

    private void processBeanDefinitions(@Nonnull final Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (final BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();
            if (log.isDebugEnabled()) {
                log.debug("Creating RetrofitClientBean with name '{}' and '{}' Interface.",
                        holder.getBeanName(), definition.getBeanClassName());
            }
            definition.getConstructorArgumentValues()
                    .addGenericArgumentValue(Objects.requireNonNull(definition.getBeanClassName()));
            // beanClass全部设置为RetrofitFactoryBean
            definition.setBeanClass(RetrofitFactoryBean.class);
        }
    }

    @Override
    protected boolean isCandidateComponent(@Nonnull final AnnotatedBeanDefinition definition) {
        final AnnotationMetadata metadata = definition.getMetadata();
        if (metadata.isInterface()) {
            try {
                final String className = metadata.getClassName();
                final Class<?> target = ClassUtils.forName(className, classLoader);
                return !target.isAnnotation();
            } catch (Exception e) {
                log.error("load class exp:", e);
            }
        }
        return false;
    }
}
