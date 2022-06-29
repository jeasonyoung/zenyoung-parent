package top.zenyoung.orm.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

/**
 * MapperScan配置
 *
 * @author young
 */
@Slf4j
@Configuration
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
public class MapperScannerRegister implements BeanFactoryAware, ImportBeanDefinitionRegistrar {
    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(@Nonnull final BeanFactory beanFactory) throws BeansException {
        log.info("beanFactory....");
        this.beanFactory = beanFactory;
    }

    @Override
    public void registerBeanDefinitions(@Nonnull final AnnotationMetadata metadata, @Nonnull final BeanDefinitionRegistry registry) {
        if (!AutoConfigurationPackages.has(beanFactory)) {
            log.warn("Could not determine auto-configuration package, automatic mapper scanning disabled.");
            return;
        }
        log.info("Searching for mappers annotated with @Mapper");
        final List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
        if (CollectionUtils.isEmpty(packages)) {
            log.warn("搜索目标包集合为空! 扫描 @Mapper 失败!");
            return;
        }
        final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
        builder.addPropertyValue("processPropertyPlaceHolders", true);
        builder.addPropertyValue("annotationClass", Mapper.class);
        builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(packages));
        final BeanWrapper beanWrapper = new BeanWrapperImpl(MapperScannerConfigurer.class);
        Stream.of(beanWrapper.getPropertyDescriptors())
                .filter(x -> "lazyInitialization".equals(x.getName()))
                .findAny()
                .ifPresent(x -> builder.addPropertyValue("lazyInitialization", "${mybatis.lazy-initialization:false}"));
        //注册Bean
        registry.registerBeanDefinition(MapperScannerConfigurer.class.getName(), builder.getBeanDefinition());
    }
}
