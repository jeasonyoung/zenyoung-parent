package top.zenyoung.boot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import javax.annotation.Nonnull;

/**
 * WebFlux-配置
 *
 * @author young
 */
@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    @Override
    public void addResourceHandlers(@Nonnull final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
