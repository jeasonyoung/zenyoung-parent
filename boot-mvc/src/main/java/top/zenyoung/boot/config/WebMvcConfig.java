package top.zenyoung.boot.config;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.zenyoung.boot.converter.EnumValueConvertFactory;
import top.zenyoung.boot.interceptor.RequestMappingInterceptor;
import top.zenyoung.boot.resolver.ArgumentResolver;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * WebMvc-配置
 *
 * @author young
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final Collection<RequestMappingInterceptor> interceptors;
    private final Collection<ArgumentResolver> argumentResolvers;

    @Value("${server.error.path:${error.path:/error}}")
    private String errorPage;

    private final Collection<String> swaggerExcludes = Lists.newArrayList(
            "/swagger-resources/**",
            "/img/**",
            "/img.icons/**",
            "/webjars/**",
            "/v2/**",
            "/v3/**",
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
                final Collection<String> includePatterns = interceptor.getIncludePatterns();
                if (!CollectionUtils.isEmpty(includePatterns)) {
                    ir.addPathPatterns(includePatterns.stream()
                            .filter(p -> !Strings.isNullOrEmpty(p))
                            .distinct()
                            .toList()
                    );
                }
                //排除匹配
                final Collection<String> excludePatterns = interceptor.getExcludePatterns();
                if (!CollectionUtils.isEmpty(swaggerExcludes)) {
                    excludePatterns.addAll(swaggerExcludes);
                }
                //排除错误
                if (!Strings.isNullOrEmpty(errorPage)) {
                    excludePatterns.add(errorPage);
                }
                if (!CollectionUtils.isEmpty(excludePatterns)) {
                    ir.excludePathPatterns(excludePatterns.stream()
                            .filter(v -> !Strings.isNullOrEmpty(v))
                            .distinct()
                            .toList()
                    );
                }
            });
        }
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
