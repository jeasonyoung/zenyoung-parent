package top.zenyoung.retrofit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.converter.jackson.JacksonConverterFactory;
import top.zenyoung.retrofit.core.BasicTypeConverterFactory;
import top.zenyoung.retrofit.log.LogProperty;
import top.zenyoung.retrofit.retry.RetryProperty;

/**
 * Retrofit-配置项
 *
 * @author young
 */
@Data
@ConfigurationProperties(prefix = "top.zenyoung.retrofit")
public class RetrofitProperties {
    /**
     * 超时配置
     */
    private TimeoutProperty timeout = new TimeoutProperty();
    /**
     * 重试配置
     */
    private RetryProperty retry = new RetryProperty();
    /**
     * 日志配置
     */
    private LogProperty log = new LogProperty();
    /**
     * 全局转换器工厂
     * <p>
     * 转换器实例优先从Spring容器获取,如果没有获取到,则反射创建
     * </p>
     */
    @SuppressWarnings({"unchecked"})
    private Class<? extends Converter.Factory>[] converterFactories = new Class[]{
            BasicTypeConverterFactory.class,
            JacksonConverterFactory.class
    };
    /**
     * 全局调用适配器工厂
     * <p>
     * 转换器实例优先从Spring容器获取,如果没有获取到,则反射创建
     * </p>
     */
    private Class<? extends CallAdapter.Factory>[] callAdapterFactories;
}
