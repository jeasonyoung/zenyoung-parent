package top.zenyoung.webclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * WebClient工具类
 *
 * @author yangyong
 * @version 1.0
 * date 2020/6/23 10:33 上午
 **/
@Slf4j
public class WebClientUtils {
    private static final int TIMEOUT = 10;
    private final OkHttpClient client;

    private WebClientUtils() {
        //网络日志
        final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLogging());
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        //初始化客户端
        this.client = new OkHttpClient.Builder()
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .addNetworkInterceptor(loggingInterceptor)
                .build();
    }

    /**
     * 获取实例对象
     *
     * @return 实例对象
     */
    public static WebClientUtils getInstance() {
        return new WebClientUtils();
    }

    /**
     * 发送请求处理
     *
     * @param method          请求方法
     * @param url             请求地址
     * @param headers         请求头参数
     * @param bodyConvert     请求体转换处理
     * @param respBodyConvert 响应数据转换处理
     * @param <R>             返回数据类型
     * @return 返回数据
     * @throws IOException 异常
     */
    public <R> R sendRequest(@Nonnull final String method, @Nonnull final String url,
                             @Nullable final Map<String, Serializable> headers,
                             @Nonnull final Supplier<RequestBody> bodyConvert, @Nonnull final Function<String, R> respBodyConvert) throws IOException {
        log.debug("sendRequest(method: {},url: {},headers: {},bodyConvert: {},respBodyConvert: {})...", method, url, headers, bodyConvert, respBodyConvert);
        final Request.Builder builder = new Request.Builder().url(url);
        //headers
        if (headers != null && headers.size() > 0) {
            headers.forEach((key, value) -> {
                if (!Strings.isNullOrEmpty(key) && value != null) {
                    builder.addHeader(key, value.toString());
                }
            });
        }
        //方法处理
        builder.method(method, bodyConvert.get());
        final Response response = client.newCall(builder.build()).execute();
        if (!response.isSuccessful()) {
            throw new RuntimeException("[" + response.code() + "]" + response.message());
        }
        final ResponseBody respBody = response.body();
        //结果转换
        return respBodyConvert.apply(respBody == null ? null : respBody.string());
    }

    /**
     * 发送JSON请求
     *
     * @param method    请求方法
     * @param url       请求地址
     * @param headers   请求头参数
     * @param body      请求报文体
     * @param objMapper JSON处理器
     * @param respClass 响应类型Class
     * @param <T>       请求报文体类型
     * @param <R>       响应数据类型
     * @return 响应数据
     * @throws IOException 异常
     */
    public <T, R> R sendJson(@Nonnull final String method, @Nonnull final String url,
                             @Nullable final Map<String, Serializable> headers, @Nullable final T body,
                             @Nonnull final ObjectMapper objMapper, @Nonnull final Class<R> respClass) throws IOException {
        log.debug("sendJson(method: {},url: {},headers: {},body: {},objMapper: {},respClass: {})...", method, url, headers, body, objMapper, respClass);
        return sendRequest(method, url, headers, () -> {
            if (body != null) {
                try {
                    final String bodyJson = objMapper.writeValueAsString(body);
                    return RequestBody.create(MediaType.parse("application/json;charset=utf-8"), bodyJson);
                } catch (Throwable ex) {
                    log.error("sendJson-writeValueAsString(body: {})-exp: {}", body, ex.getMessage());
                    throw new RuntimeException(ex);
                }
            }
            return null;
        }, resp -> {
            if (!Strings.isNullOrEmpty(resp)) {
                try {
                    return objMapper.readValue(resp, respClass);
                } catch (Throwable ex) {
                    log.error("sendPostJson-readValue(resp: {})-exp: {}", resp, ex.getMessage());
                    throw new RuntimeException(ex);
                }
            }
            return null;
        });
    }

    /**
     * 发送GET-JSON请求
     *
     * @param url       请求地址
     * @param headers   请求头参数
     * @param objMapper JSON处理器
     * @param respClass 响应类型Class
     * @param <R>       响应数据类型
     * @return 响应数据
     * @throws IOException 异常
     */
    public <R> R sendGetJson(@Nonnull final String url, @Nullable final Map<String, Serializable> headers,
                             @Nonnull final ObjectMapper objMapper, @Nonnull final Class<R> respClass) throws IOException {
        log.debug("sendGetJson(url: {},headers: {},objMapper: {},respClass: {})...", url, headers, objMapper, respClass);
        return sendJson("GET", url, headers, null, objMapper, respClass);
    }

    /**
     * 发送POST-JSON请求
     *
     * @param url       请求地址
     * @param headers   请求头参数
     * @param body      请求报文体
     * @param objMapper JSON处理器
     * @param respClass 响应类型Class
     * @param <T>       请求报文体类型
     * @param <R>       响应数据类型
     * @return 响应数据
     * @throws IOException 异常
     */
    public <T, R> R sendPostJson(@Nonnull final String url, @Nullable final Map<String, Serializable> headers, @Nullable final T body,
                                 @Nonnull final ObjectMapper objMapper, @Nonnull final Class<R> respClass) throws IOException {
        log.debug("sendPostJson(url: {},headers: {},body: {},objMapper: {},respClass: {})...", url, headers, body, objMapper, respClass);
        return sendJson("POST", url, headers, body, objMapper, respClass);
    }

    /**
     * 发送PUT-JSON请求
     *
     * @param url       请求地址
     * @param headers   请求头参数
     * @param body      请求报文体
     * @param objMapper JSON处理器
     * @param respClass 响应类型Class
     * @param <T>       请求报文体类型
     * @param <R>       响应数据类型
     * @return 响应数据
     * @throws IOException 异常
     */
    public <T, R> R sendPutJson(@Nonnull final String url, @Nullable final Map<String, Serializable> headers, @Nullable final T body,
                                @Nonnull final ObjectMapper objMapper, @Nonnull final Class<R> respClass) throws IOException {
        log.debug("sendPutJson(url: {},headers: {},body: {},objMapper: {},respClass: {})...", url, headers, body, objMapper, respClass);
        return sendJson("PUT", url, headers, body, objMapper, respClass);
    }

    /**
     * 发送DELETE-JSON请求
     *
     * @param url       请求地址
     * @param headers   请求头参数
     * @param objMapper JSON处理器
     * @param respClass 响应类型Class
     * @param <R>       响应数据类型
     * @return 响应数据
     * @throws IOException 异常
     */
    public <R> R sendDeleteJson(@Nonnull final String url, @Nullable final Map<String, Serializable> headers,
                                @Nonnull final ObjectMapper objMapper, @Nonnull final Class<R> respClass) throws IOException {
        log.debug("sendDeleteJson(url: {},headers: {},objMapper: {},respClass: {})...", url, headers, objMapper, respClass);
        return sendJson("DELETE", url, headers, null, objMapper, respClass);
    }

    private static class HttpLogging implements HttpLoggingInterceptor.Logger {

        @Override
        public void log(@Nonnull final String s) {
            log.info(s);
        }
    }
}
