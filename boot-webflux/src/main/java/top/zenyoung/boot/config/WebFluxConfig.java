package top.zenyoung.boot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import top.zenyoung.boot.converter.EnumValueConvertFactory;
import top.zenyoung.boot.resolver.ArgumentResolver;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * WebFlux-配置
 *
 * @author young
 */
@Configuration
@EnableWebFlux
@RequiredArgsConstructor
public class WebFluxConfig implements WebFluxConfigurer {
    private final List<ArgumentResolver> argumentResolvers;

    @Override
    public void addFormatters(@Nonnull final FormatterRegistry registry) {
        registry.addConverterFactory(EnumValueConvertFactory.of());
    }

    @Override
    public void configureArgumentResolvers(@Nonnull final ArgumentResolverConfigurer configurer) {
        //自定义参数处理器
        final List<ArgumentResolver> resolvers;
        if (!CollectionUtils.isEmpty(resolvers = argumentResolvers)) {
            configurer.addCustomResolver(resolvers.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toArray(ArgumentResolver[]::new)
            );
        }
    }
}
