package top.zenyoung.retrofit.core;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import okhttp3.OkHttpClient;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import top.zenyoung.retrofit.FallbackFactory;
import top.zenyoung.retrofit.annotation.RetrofitClient;
import top.zenyoung.retrofit.config.TimeoutProperty;
import top.zenyoung.retrofit.exception.RetrofitException;
import top.zenyoung.retrofit.interceptor.GlobalInterceptor;
import top.zenyoung.retrofit.interceptor.NetworkInterceptor;
import top.zenyoung.retrofit.util.ContextUtils;
import top.zenyoung.retrofit.util.RetrofitUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Retrofit Factory Bean
 *
 * @param <T> 类型
 * @author young
 */
public class RetrofitFactoryBean<T> implements FactoryBean<T>, EnvironmentAware, ApplicationContextAware {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private final Class<T> retrofitInterface;
    private Environment env;
    private ApplicationContext context;
    private RetrofitConfigBean configBean;

    public RetrofitFactoryBean(@Nonnull final Class<T> retrofitInterface) {
        this.retrofitInterface = retrofitInterface;
    }

    @Override
    public T getObject() {
        final T source = createRetrofit().create(retrofitInterface);
        final RetrofitClient client = AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, RetrofitClient.class);
        final Class<?> fallbackFactory;
        if (Objects.nonNull(client) && Objects.nonNull(fallbackFactory = client.fallbackFactory()) && !Void.class.isAssignableFrom(fallbackFactory)) {
            @SuppressWarnings({"unchecked"}) final Class<FallbackFactory<?>> factoryClass = (Class<FallbackFactory<?>>) fallbackFactory;
            return FallbackFactoryProxy.create(retrofitInterface, factoryClass, source, context);
        }
        return source;
    }

    private Retrofit createRetrofit() {
        final RetrofitClient retrofitClient = AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, RetrofitClient.class);
        if (Objects.isNull(retrofitClient)) {
            throw new RetrofitException(retrofitInterface + " not annotated @RetrofitClient");
        }
        final String baseUrl = RetrofitUtils.convertBaseUrl(retrofitClient, env);
        final OkHttpClient httpClient = createOkHttpClient(retrofitClient);
        final Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .validateEagerly(retrofitClient.validateEagerly())
                .client(httpClient);
        final Predicate<Class<?>[]> checkClassArray = array -> array != null && array.length > 0;
        // 添加配置或者指定的CallAdapterFactory
        final List<Class<? extends CallAdapter.Factory>> callAdapterFactories = Lists.newArrayList();
        if (checkClassArray.test(retrofitClient.callAdapterFactories())) {
            callAdapterFactories.addAll(Arrays.asList(retrofitClient.callAdapterFactories()));
        }
        if (checkClassArray.test(configBean.getGlobalCallAdapterFactoryClasses())) {
            callAdapterFactories.addAll(Arrays.asList(configBean.getGlobalCallAdapterFactoryClasses()));
        }
        if (!CollectionUtils.isEmpty(callAdapterFactories)) {
            callAdapterFactories.stream()
                    // 过滤掉内置的CallAdapterFactory，因为后续会指定add
                    .filter(fc -> !InternalCallAdapterFactory.class.isAssignableFrom(fc))
                    .map(fc -> ContextUtils.getBeanOrNew(context, fc))
                    .filter(Objects::nonNull)
                    .forEach(builder::addCallAdapterFactory);
        }
        builder.addCallAdapterFactory(ResponseCallAdapterFactory.INSTANCE);
        builder.addCallAdapterFactory(BodyCallAdapterFactory.INSTANCE);
        //添加配置或者指定的ConverterFactory
        final List<Class<? extends Converter.Factory>> converterFactories = Lists.newArrayList();
        if (checkClassArray.test(retrofitClient.converterFactories())) {
            converterFactories.addAll(Lists.newArrayList(retrofitClient.converterFactories()));
        }
        if (checkClassArray.test(configBean.getGlobalConverterFactoryClasses())) {
            converterFactories.addAll(Lists.newArrayList(configBean.getGlobalConverterFactoryClasses()));
        }
        if (!CollectionUtils.isEmpty(converterFactories)) {
            converterFactories.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(fc -> ContextUtils.getBeanOrNew(context, fc))
                    .filter(Objects::nonNull)
                    .forEach(builder::addConverterFactory);
        }
        return builder.build();
    }

    private OkHttpClient createOkHttpClient(@Nonnull final RetrofitClient retrofitClient) {
        final String okHttpClientKey = buildOkHttpClientKey(retrofitClient);
        synchronized (LOCKS.computeIfAbsent(okHttpClientKey, k -> new Object())) {
            try {
                OkHttpClient.Builder builder;
                final OkHttpClientRegistry clientRegistry = configBean.getOkHttpClientRegistry();
                OkHttpClient client = clientRegistry.get(okHttpClientKey);
                if (Objects.nonNull(client)) {
                    builder = client.newBuilder();
                } else {
                    //默认超时
                    final TimeoutProperty timeout = configBean.getProperties().getTimeout();
                    final int connectTimeoutMs = timeout.getConnectTimeoutMs();
                    final int readTimeoutMs = timeout.getReadTimeoutMs();
                    final int writeTimeoutMs = timeout.getWriteTimeoutMs();
                    final int callTimeoutMs = timeout.getCallTimeoutMs();
                    //创建
                    builder = new OkHttpClient.Builder()
                            .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                            .readTimeout(Duration.ofMillis(readTimeoutMs))
                            .writeTimeout(Duration.ofMillis(writeTimeoutMs))
                            .callTimeout(Duration.ofMillis(callTimeoutMs));
                }
                //1.服务ID拦截器
                if (StringUtils.hasText(retrofitClient.serviceId())) {
                    builder.addInterceptor(configBean.getServiceChooseInterceptor());
                }
                //2.错误解码拦截器
                builder.addInterceptor(configBean.getErrorDecoderInterceptor());
                //3.全局拦截器集合
                addGlobalInterceptors(builder, configBean.getGlobalInterceptors());
                //4.重试拦截器
                builder.addInterceptor(configBean.getRetryInterceptor());
                //5.日志拦截器
                builder.addInterceptor(configBean.getLoggingInterceptor());
                //6.网络拦截器集合
                addNetworkInterceptors(builder, configBean.getNetworkInterceptors());
                //生成OkHttp对象
                return builder.build();
            } finally {
                LOCKS.remove(okHttpClientKey);
            }
        }
    }

    private String buildOkHttpClientKey(@Nonnull final RetrofitClient client) {
        if (!Strings.isNullOrEmpty(client.okHttpClient())) {
            return client.okHttpClient();
        }
        final UnaryOperator<String> createKeyHandler = key -> {
            key = env.resolveRequiredPlaceholders(key);
            return DigestUtils.md5Hex(key);
        };
        if (!Strings.isNullOrEmpty(client.baseUrl())) {
            return createKeyHandler.apply(client.baseUrl());
        }
        if (!Strings.isNullOrEmpty(client.serviceId())) {
            return createKeyHandler.apply(client.serviceId());
        }
        return "default";
    }

    private void addGlobalInterceptors(@Nonnull final OkHttpClient.Builder builder, @Nullable final List<GlobalInterceptor> interceptors) {
        if (!CollectionUtils.isEmpty(interceptors)) {
            interceptors.stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(GlobalInterceptor::order))
                    .forEach(builder::addInterceptor);
        }
    }

    private void addNetworkInterceptors(@Nonnull final OkHttpClient.Builder builder, @Nullable final List<NetworkInterceptor> interceptors) {
        if (!CollectionUtils.isEmpty(interceptors)) {
            interceptors.stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(NetworkInterceptor::order))
                    .forEach(builder::addNetworkInterceptor);
        }
    }

    @Override
    public Class<T> getObjectType() {
        return this.retrofitInterface;
    }

    @Override
    public void setApplicationContext(@Nonnull final ApplicationContext context) throws BeansException {
        this.context = context;
        this.configBean = context.getBean(RetrofitConfigBean.class);
    }

    @Override
    public void setEnvironment(@Nonnull final Environment env) {
        this.env = env;
    }
}
