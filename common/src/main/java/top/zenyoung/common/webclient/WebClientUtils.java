package top.zenyoung.common.webclient;

import com.google.common.base.Strings;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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

    @SneakyThrows({NoSuchAlgorithmException.class, KeyManagementException.class})
    private WebClientUtils() {
        //网络日志
        final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(content -> {
            if (!Strings.isNullOrEmpty(content)) {
                log.info(content);
            }
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        //SSL
        final X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        //初始化客户端
        this.client = new OkHttpClient.Builder()
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .addNetworkInterceptor(loggingInterceptor)
                .sslSocketFactory(sslSocketFactory, trustManager)
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
    public <R> R sendRequest(
            @Nonnull final String method,
            @Nonnull final String url,
            @Nullable final Map<String, Serializable> headers,
            @Nonnull final Supplier<RequestBody> bodyConvert,
            @Nonnull final Function<String, R> respBodyConvert
    ) throws IOException {
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
        try (final Response response = client.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("[" + response.code() + "]" + response.message());
            }
            final ResponseBody respBody = response.body();
            //结果转换
            return respBodyConvert.apply(respBody == null ? null : respBody.string());
        }
    }

    @Override
    public void downloadFile(@Nonnull final String url, @Nonnull final OutputStream outputStream, @Nullable final Consumer<Integer> progress) {
        log.debug("downloadFile(url: {},progress: {})...", url, progress);
        final long start = System.currentTimeMillis();
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Connection", "close")
                .build();
        client.newCall(request)
                .enqueue(new Callback() {

                    @Override
                    public void onFailure(@Nonnull final Call call, @Nonnull final IOException e) {
                        log.error("downloadFile(url: {},progress: {})-exp: {}", url, progress, e.getMessage());
                    }

                    @Override
                    public void onResponse(@Nonnull final Call call, @Nonnull final Response response) throws IOException {
                        final ResponseBody respBody = response.body();
                        if (respBody != null) {
                            final long total = respBody.contentLength();
                            try (final InputStream is = respBody.byteStream()) {
                                final byte[] buf = new byte[1024 * 4];
                                long sum = 0, len;
                                while ((len = is.read(buf, 0, buf.length)) != -1) {
                                    outputStream.write(buf, 0, (int) len);
                                    sum += len;
                                    if (progress != null) {
                                        progress.accept((int) ((sum * 1.0f / total) * 100));
                                    }
                                }
                                //写入文件
                                outputStream.flush();
                            }
                            log.info("downloadFile(url: {},progress: {})-下载完成![耗时: {}ms]", url, progress, (System.currentTimeMillis() - start));
                        }
                    }
                });
    }

    @SneakyThrows({IOException.class})
    @Override
    public void uploadFile(@Nonnull final String url, @Nullable final Map<String, Serializable> headers, @Nonnull final Consumer<MultipartBody.Builder> bodyBuilderHandler) {
        log.debug("uploadFile(url: {},headers: {},bodyBuilderHandler: {})...", url, headers, bodyBuilderHandler);
        //上传请求
        final Request.Builder requestBuilder = new Request.Builder().url(url);
        //headers参数
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((key, val) -> {
                if (!Strings.isNullOrEmpty(key) && val != null) {
                    requestBuilder.addHeader(key, val.toString());
                }
            });
        }
        final MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        //上传参数处理
        bodyBuilderHandler.accept(bodyBuilder);
        //上传数据构建
        requestBuilder.post(bodyBuilder.build());
        //上传处理
        try (final Response response = client.newCall(requestBuilder.build()).execute()) {
            if (response.isSuccessful()) {
                final ResponseBody respBody = response.body();
                if (respBody != null) {
                    log.info("uploadFile(url: {},headers: {},bodyBuilderHandler: {})=> {}", url, headers, bodyBuilderHandler, respBody.string());
                }
            }
        }
    }
}
