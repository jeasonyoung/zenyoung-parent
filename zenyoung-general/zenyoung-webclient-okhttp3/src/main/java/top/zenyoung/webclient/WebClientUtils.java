package top.zenyoung.webclient;

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
public class WebClientUtils implements WebClient {
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
    @Override
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

    private static class HttpLogging implements HttpLoggingInterceptor.Logger {

        @Override
        public void log(@Nonnull final String s) {
            log.info(s);
        }
    }
}
