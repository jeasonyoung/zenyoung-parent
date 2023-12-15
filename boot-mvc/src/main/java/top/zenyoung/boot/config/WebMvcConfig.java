package top.zenyoung.boot.config;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.*;
import top.zenyoung.boot.converter.EnumValueConvertFactory;
import top.zenyoung.boot.interceptor.RequestMappingInterceptor;
import top.zenyoung.boot.resolver.ArgumentResolver;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * WebMvc-配置
 *
 * @author young
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final List<RequestMappingInterceptor> interceptors;
    private final List<ArgumentResolver> argumentResolvers;

    @Value("${server.error.path:${error.path:/error}}")
    private String errorPage;

    private final List<String> swaggerExcludes = Lists.newArrayList(
            "/swagger-resources/**",
            "/webjars/**",
            "/v2/**",
            "/favicon.ico",
            "/swagger-ui.html/**",
            "/code.html",
            "/doc.html"
    );

    @Override
    public void addInterceptors(@Nonnull final InterceptorRegistry registry) {
        //拦截器
        if (!CollectionUtils.isEmpty(this.interceptors)) {
            this.interceptors.forEach(interceptor -> {
                final InterceptorRegistration ir = registry.addInterceptor(interceptor);
                //排序
                ir.order(interceptor.getOrder());
                //包括匹配
                final List<String> includePatterns = interceptor.getIncludePatterns();
                if (!CollectionUtils.isEmpty(includePatterns)) {
                    ir.addPathPatterns(includePatterns);
                }
                //排除匹配
                final List<String> excludePatterns = interceptor.getExcludePatterns();
                if (!CollectionUtils.isEmpty(swaggerExcludes)) {
                    excludePatterns.addAll(swaggerExcludes);
                }
                //排除错误
                if (!Strings.isNullOrEmpty(errorPage)) {
                    excludePatterns.add(errorPage);
                }
                if (!CollectionUtils.isEmpty(excludePatterns)) {
                    ir.excludePathPatterns(excludePatterns);
                }
            });
        }
    }

    @Override
    public void addViewControllers(@Nonnull final ViewControllerRegistry registry) {
        registry.addRedirectViewController("/null/api-docs", "/api-docs").setKeepQueryParams(true);
        registry.addRedirectViewController("/null/v2/api-docs", "/v2/api-docs").setKeepQueryParams(true);
        registry.addRedirectViewController("/null/swagger-resources/configuration/ui", "/swagger-resources/configuration/ui");
        registry.addRedirectViewController("/null/swagger-resources/configuration/security", "/swagger-resources/configuration/security");
        registry.addRedirectViewController("/null/swagger-resources", "/swagger-resources");
    }

    @Override
    public void addResourceHandlers(@Nonnull final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addArgumentResolvers(@Nonnull final List<HandlerMethodArgumentResolver> resolvers) {
        //参数分解器
        if (!CollectionUtils.isEmpty(this.argumentResolvers)) {
            resolvers.addAll(this.argumentResolvers);
        }
    }

    @Override
    public void addFormatters(@Nonnull final FormatterRegistry registry) {
        registry.addConverterFactory(EnumValueConvertFactory.of());
    }
}
