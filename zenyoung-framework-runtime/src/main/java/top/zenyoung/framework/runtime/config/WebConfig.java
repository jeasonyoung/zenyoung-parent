package top.zenyoung.framework.runtime.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Nonnull;

/**
 * Web配置
 *
 * @author young
 */
@EnableWebMvc
@Configuration
@ConditionalOnMissingBean(WebMvcConfigurer.class)
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@Nonnull final CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods("*")
                .maxAge(3600L);
    }
}
