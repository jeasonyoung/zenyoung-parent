package top.zenyoung.boot.registrar;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import top.zenyoung.boot.annotation.Boot;
import top.zenyoung.boot.util.RegistrarUtils;

import javax.annotation.Nonnull;

/**
 * 指定包注解类自动注入
 *
 * @author young
 */
public class BootRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(@Nonnull final AnnotationMetadata metadata, @Nonnull final BeanDefinitionRegistry registry) {
        RegistrarUtils.registerBeanDefinitions(metadata, registry, Boot.class);
    }
}
