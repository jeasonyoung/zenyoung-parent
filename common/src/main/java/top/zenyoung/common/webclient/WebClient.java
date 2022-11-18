package top.zenyoung.common.webclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * WebClient-工具接口
 *
 * @author yangyong
 * @version 1.0
 * date 2020/6/23 2:09 下午
 **/
public interface WebClient {

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
    <R> R sendRequest(@Nonnull final String method, @Nonnull final String url,
                      @Nullable final Map<String, Serializable> headers,
                      @Nonnull final Supplier<RequestBody> bodyConvert, @Nonnull final Function<String, R> respBodyConvert) throws IOException;

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
    default <T, R> R sendJson(@Nonnull final String method, @Nonnull final String url,
                              @Nullable final Map<String, Serializable> headers, @Nullable final T body,
                              @Nonnull final ObjectMapper objMapper, @Nonnull final Class<R> respClass) throws IOException {
        return sendRequest(method, url, headers, () -> {
            if (body != null) {
                try {
                    final String bodyJson = objMapper.writeValueAsString(body);
                    return RequestBody.create(MediaType.parse("application/json;charset=utf-8"), bodyJson);
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
            }
            return null;
        }, resp -> {
            if (!Strings.isNullOrEmpty(resp)) {
                try {
                    return objMapper.readValue(resp, respClass);
                } catch (Throwable ex) {
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
    default <R> R sendGetJson(@Nonnull final String url, @Nullable final Map<String, Serializable> headers,
                              @Nonnull final ObjectMapper objMapper, @Nonnull final Class<R> respClass) throws IOException {
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
    default <T, R> R sendPostJson(@Nonnull final String url, @Nullable final Map<String, Serializable> headers, @Nullable final T body,
                                  @Nonnull final ObjectMapper objMapper, @Nonnull final Class<R> respClass) throws IOException {
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
    default <T, R> R sendPutJson(@Nonnull final String url, @Nullable final Map<String, Serializable> headers, @Nullable final T body,
                                 @Nonnull final ObjectMapper objMapper, @Nonnull final Class<R> respClass) throws IOException {
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
    default <R> R sendDeleteJson(@Nonnull final String url, @Nullable final Map<String, Serializable> headers,
                                 @Nonnull final ObjectMapper objMapper, @Nonnull final Class<R> respClass) throws IOException {
        return sendJson("DELETE", url, headers, null, objMapper, respClass);
    }

    /**
     * 下载文件
     *
     * @param url          下载地址URL
     * @param outputStream 存储流
     * @param progress     下载进度
     */
    void downloadFile(@Nonnull final String url, @Nonnull final OutputStream outputStream, @Nullable final Consumer<Integer> progress);

    /**
     * 上传文件
     *
     * @param url                上传地址
     * @param headers            上传消息头
     * @param bodyBuilderHandler 上传处理
     */
    void uploadFile(@Nonnull final String url, @Nullable final Map<String, Serializable> headers, @Nonnull final Consumer<MultipartBody.Builder> bodyBuilderHandler);
}
