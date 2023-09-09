package top.zenyoung.retrofit.core;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import top.zenyoung.retrofit.config.RetrofitProperties;
import top.zenyoung.retrofit.interceptor.ErrorDecoderInterceptor;
import top.zenyoung.retrofit.interceptor.GlobalInterceptor;
import top.zenyoung.retrofit.interceptor.NetworkInterceptor;
import top.zenyoung.retrofit.interceptor.ServiceChooseInterceptor;
import top.zenyoung.retrofit.log.LoggingInterceptor;
import top.zenyoung.retrofit.retry.RetryInterceptor;

import java.util.List;

/**
 * 配置对象Bean
 *
 * @author young
 */
@Data
@RequiredArgsConstructor(staticName = "of")
public class RetrofitConfigBean {
    /**
     * 配置
     */
    private final RetrofitProperties properties;
    /**
     * 全局拦截器
     */
    private List<GlobalInterceptor> globalInterceptors;
    /**
     * 全局网络拦截器
     */
    private List<NetworkInterceptor> networkInterceptors;
    /**
     * 重试拦截器
     */
    private RetryInterceptor retryInterceptor;
    /**
     * 服务选择拦截器
     */
    private ServiceChooseInterceptor serviceChooseInterceptor;
    /**
     * 全局转换工厂类集合
     */
    private Class<? extends Converter.Factory>[] globalConverterFactoryClasses;
    /**
     * 全局适配器工厂类集合
     */
    private Class<? extends CallAdapter.Factory>[] globalCallAdapterFactoryClasses;
    /**
     * 日志拦截器
     */
    private LoggingInterceptor loggingInterceptor;
    /**
     * 错误解码拦截器
     */
    private ErrorDecoderInterceptor errorDecoderInterceptor;
    /**
     * okHttpClient注册中心
     */
    private OkHttpClientRegistry okHttpClientRegistry;
}
