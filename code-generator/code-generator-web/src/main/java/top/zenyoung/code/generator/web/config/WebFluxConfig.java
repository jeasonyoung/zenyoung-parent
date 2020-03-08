package top.zenyoung.code.generator.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.ViewResolverRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.thymeleaf.spring5.view.reactive.ThymeleafReactiveViewResolver;

/**
 * WebFlux-配置
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/3/8 4:49 下午
 **/
@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {

    @Autowired
    private ThymeleafReactiveViewResolver viewResolver = null;

    @Override
    public void configureViewResolvers(final ViewResolverRegistry registry) {
        registry.viewResolver(viewResolver);
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
