package top.zenyoung.retrofit.annotation;

import retrofit2.CallAdapter;
import retrofit2.Converter;
import top.zenyoung.retrofit.ErrorDecoder;

import java.lang.annotation.*;

/**
 * Retrofit-客户端_注解
 *
 * @author young
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RetrofitClient {
    /**
     * 基础URL(协议是必需的)
     * <p>
     * 可以指定为属性键,例如: ${propertyKey}.
     * 如果baseUrl没有配置,则必须配置serviceId以及path可选配置
     * </p>
     *
     * @return 基础URL
     */
    String baseUrl() default "";

    /**
     * 服务ID
     * <p>
     * 可以指定为属性键,例如: ${propertyKey}.
     * </p>
     *
     * @return 服务ID
     */
    String serviceId() default "";

    /**
     * 统一路径前缀
     * <p>
     * Path prefix to be used by all method-level mappings.
     * </p>
     *
     * @return 路径前缀
     */
    String path() default "";

    /**
     * 适用于当前接口的转换器工厂
     * <p>
     * 优先级比全局转换器工厂更高
     * 转换器实例优先从Spring容器获取
     * 如果没有获取到,则反射创建
     * </p>
     *
     * @return 转换器工厂
     */
    Class<? extends Converter.Factory>[] converterFactories() default {};

    /**
     * 适用于当前接口的调用适配器工厂
     * <p>
     * 优先级比全局调用适配器工厂更高
     * 转换器实例优先从Spring容器获取
     * 如果没有获取到,则反射创建
     * </p>
     *
     * @return 适配器工厂
     */
    Class<? extends CallAdapter.Factory>[] callAdapterFactories() default {};

    /**
     * Define a fallback factory for the specified Feign client interface. The fallback
     * factory must produce instances of fallback classes that implement the interface
     * annotated by {@link RetrofitClient}.The fallback factory must be a valid spring bean.
     *
     * @return fallback factory
     */
    Class<?> fallbackFactory() default Void.class;

    /**
     * 当前接口采用的错误解码器，当请求发生异常或者收到无效响应结果的时候，将HTTP相关信息解码到异常中，无效响应由业务自己判断。
     * 一般情况下，每个服务对应的无效响应各不相同，可以自定义对应的{@link ErrorDecoder}，然后配置在这里.
     *
     * @return 错误解码器
     */
    Class<? extends ErrorDecoder> errorDecoder() default ErrorDecoder.DefaultErrorDecoder.class;

    /**
     * When calling {@link retrofit2.Retrofit#create(Class)} on the resulting {@link retrofit2.Retrofit} instance, eagerly validate the
     * configuration of all methods in the supplied interface.
     *
     * @return validateEagerly
     */
    boolean validateEagerly() default false;

    /**
     * 源OkHttpClient,根据该名称到#{@link top.zenyoung.retrofit.core.OkHttpClientRegistry}
     * 查找对应的 OkHttpClient 来构建当前接口的 OkhttpClient
     *
     * @return 源OkHttpClient
     */
    String okHttpClient() default "";
}
