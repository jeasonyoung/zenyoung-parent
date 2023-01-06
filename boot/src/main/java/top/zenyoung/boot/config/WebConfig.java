package top.zenyoung.boot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.zenyoung.boot.interceptor.RequestMappingInterceptor;
import top.zenyoung.boot.resolver.ArgumentResolver;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Web配置
 *
 * @author young
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired(required = false)
    private List<RequestMappingInterceptor> interceptors;
    @Autowired(required = false)
    private List<ArgumentResolver> argumentResolvers;

    @Override
    public void addInterceptors(@Nonnull final InterceptorRegistry registry) {
        //拦截器
        if (!CollectionUtils.isEmpty(this.interceptors)) {
            this.interceptors.forEach(interceptor -> {
                final int order = interceptor.getOrder();
                final List<String> includePatterns = interceptor.getIncludePatterns(), excludePatterns = interceptor.getExcludePatterns();
                final InterceptorRegistration ir = registry.addInterceptor(interceptor);
                if (order != 0) {
                    ir.order(order);
                }
                if (!CollectionUtils.isEmpty(includePatterns)) {
                    ir.addPathPatterns(includePatterns);
                }
                if (!CollectionUtils.isEmpty(excludePatterns)) {
                    ir.excludePathPatterns(excludePatterns);
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

    @RequiredArgsConstructor(staticName = "of")
    private static class EnumValueConvertFactory implements ConverterFactory<Integer, EnumValue> {

        @Override
        @SuppressWarnings({"all"})
        public <T extends EnumValue> Converter<Integer, T> getConverter(@Nonnull final Class<T> targetType) {
            return source -> {
                final T[] enums = targetType.getEnumConstants();
                for (final T e : enums) {
                    if (e.getVal() == source) {
                        return e;
                    }
                }
                return null;
            };
        }
    }
}
