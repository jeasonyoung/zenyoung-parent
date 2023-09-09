package top.zenyoung.retrofit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import retrofit2.converter.jackson.JacksonConverterFactory;
import top.zenyoung.retrofit.config.RetrofitProperties;
import top.zenyoung.retrofit.core.*;
import top.zenyoung.retrofit.interceptor.*;
import top.zenyoung.retrofit.log.AggregateLoggingInterceptor;
import top.zenyoung.retrofit.log.LoggingInterceptor;
import top.zenyoung.retrofit.retry.RetryInterceptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

/**
 * Retrofit-自动配置
 *
 * @author young
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({RetrofitProperties.class})
public class RetrofitAutoConfiguration {
    private final RetrofitProperties properties;

    /**
     * ObjectMap 处理器
     *
     * @return JSON处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * 拦截器Scope处理器
     *
     * @return 处理器
     */
    @Bean
    public InterceptorRegistryPostProcessor interceptorRegistryPostProcessor() {
        return new InterceptorRegistryPostProcessor();
    }

    /**
     * 基础类型转换器
     *
     * @return 基础类型转换器
     */
    @Bean
    public BasicTypeConverterFactory basicTypeConverterFactory() {
        return BasicTypeConverterFactory.INSTANCE;
    }

    /**
     * OkHttpClient 注册中心
     *
     * @param registrars 注册器集合
     * @return 注册中心
     */
    @Bean
    @ConditionalOnMissingBean
    public OkHttpClientRegistry okHttpClientRegistry(@Nullable final List<OkHttpClientRegistrar> registrars) {
        return new OkHttpClientRegistry(registrars);
    }

    /**
     * 默认错误解码器
     *
     * @return 错误解码器
     */
    @Bean
    @ConditionalOnMissingBean
    public ErrorDecoder.DefaultErrorDecoder defaultErrorDecoder() {
        return new ErrorDecoder.DefaultErrorDecoder();
    }

    /**
     * 错误解码拦截器
     *
     * @return 错误解码拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public ErrorDecoderInterceptor errorDecoderInterceptor() {
        return new ErrorDecoderInterceptor();
    }

    /**
     * 重试-拦截器
     *
     * @return 重试拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryInterceptor retryInterceptor() {
        return new RetryInterceptor(properties.getRetry());
    }

    /**
     * 日志-拦截器
     *
     * @return 日志拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public LoggingInterceptor loggingInterceptor() {
        return new AggregateLoggingInterceptor(properties.getLog());
    }

    /**
     * 服务实例选择器
     *
     * @return 服务实例选择器
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceInstanceChooser serviceInstanceChooser() {
        return new ServiceInstanceChooser.NoValidServiceInstanceChooser();
    }

    /**
     * 服务选择拦截器
     *
     * @param chooser 服务实例选择器
     * @return 服务选择拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceChooseInterceptor serviceChooseInterceptor(@Nonnull final ServiceInstanceChooser chooser) {
        return new ServiceChooseInterceptor(chooser);
    }

    /**
     * Json 转换工厂
     *
     * @param objMapper JSON处理器
     * @return 转换工厂
     */
    @Bean
    @ConditionalOnMissingBean
    public JacksonConverterFactory jacksonConverterFactory(@Nonnull final ObjectMapper objMapper) {
        return JacksonConverterFactory.create(objMapper);
    }


    @Bean
    @ConditionalOnMissingBean
    public RetrofitConfigBean retrofitConfigBean(@Nullable final List<GlobalInterceptor> globalInterceptors,
                                                 @Nullable final List<NetworkInterceptor> networkInterceptors,
                                                 @Nonnull final RetryInterceptor retryInterceptor,
                                                 @Nonnull final ServiceChooseInterceptor serviceChooseInterceptor,
                                                 @Nonnull final LoggingInterceptor loggingInterceptor,
                                                 @Nonnull final ErrorDecoderInterceptor errorDecoderInterceptor,
                                                 @Nonnull final OkHttpClientRegistry registry) {
        final RetrofitConfigBean configBean = RetrofitConfigBean.of(properties);
        //全局拦截器
        configBean.setGlobalInterceptors(globalInterceptors);
        //全局网络拦截器
        configBean.setNetworkInterceptors(networkInterceptors);
        //重试拦截器
        configBean.setRetryInterceptor(retryInterceptor);
        //服务选择拦截器
        configBean.setServiceChooseInterceptor(serviceChooseInterceptor);
        //全局转换工厂类集合
        configBean.setGlobalConverterFactoryClasses(properties.getConverterFactories());
        //全局适配器工厂类集合
        configBean.setGlobalCallAdapterFactoryClasses(properties.getCallAdapterFactories());
        //日志拦截器
        configBean.setLoggingInterceptor(loggingInterceptor);
        //错误解码拦截器
        configBean.setErrorDecoderInterceptor(errorDecoderInterceptor);
        //okHttpClient注册中心
        configBean.setOkHttpClientRegistry(registry);
        //返回
        return configBean;
    }

    @Configuration
    @Import({AutoConfiguredRetrofitScannerRegistrar.class})
    @ConditionalOnMissingBean(RetrofitFactoryBean.class)
    public static class ScannerRegistrarNotFoundConfiguration {

    }

    @Slf4j
    public static class InterceptorRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

        @Override
        public void postProcessBeanDefinitionRegistry(@Nonnull final BeanDefinitionRegistry registry) throws BeansException {
            final String[] beanDefinitionNames = registry.getBeanDefinitionNames();
            if (beanDefinitionNames.length > 0) {
                Stream.of(beanDefinitionNames)
                        .filter(name -> !Strings.isNullOrEmpty(name))
                        .distinct()
                        .forEach(beanDefinitionName -> {
                            final BeanDefinition beanDefinition = registry.getBeanDefinition(beanDefinitionName);
                            final String beanClassName = beanDefinition.getBeanClassName();
                            if (!Strings.isNullOrEmpty(beanClassName)) {
                                try {
                                    final Class<?> beanClass = Class.forName(beanClassName);
                                    if (BaseInterceptor.class.isAssignableFrom(beanClass)) {
                                        beanDefinition.setScope(ConfigurableBeanFactory.SCOPE_PROTOTYPE);
                                    }
                                } catch (ClassNotFoundException e) {
                                    log.warn("postProcessBeanDefinitionRegistry-beanClassName: {},exp", beanDefinitionName, e);
                                }
                            }
                        });
            }
        }

        @Override
        public void postProcessBeanFactory(@Nonnull final ConfigurableListableBeanFactory beanFactory) throws BeansException {

        }
    }

}
