package com.huawei.services.runtime.entity.apig;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.Data;
import org.apache.commons.codec.binary.Base64;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * 触发响应
 *
 * @author young
 */
@Data
public class APIGTriggerResponse {
    private static final int HTTP_OK = 200;
    private String body;
    private Map<String, String> headers;
    private int statusCode;
    private boolean isBase64Encoded;

    /**
     * 构造函数
     */
    public APIGTriggerResponse() {
        this.statusCode = HTTP_OK;
        this.body = "";
        this.isBase64Encoded = false;
    }

    /**
     * 构造函数
     *
     * @param statusCode 状态码
     * @param headers    响应头
     * @param body       响应报文体
     */
    public APIGTriggerResponse(final int statusCode, final Map<String, String> headers, final String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
        this.isBase64Encoded = false;
    }

    /**
     * 构造函数
     *
     * @param statusCode      状态码
     * @param headers         响应头
     * @param isBase64Encoded 是否base64编码
     * @param body            响应报文体
     */
    public APIGTriggerResponse(final int statusCode, final Map<String, String> headers, final boolean isBase64Encoded, final String body) {
        this.body = body;
        this.headers = headers;
        this.statusCode = statusCode;
        this.isBase64Encoded = isBase64Encoded;
    }

    /**
     * 设置Base64编码报文体
     *
     * @param body 报文体
     */
    public void setBase64EncodedBody(@Nonnull final String body) {
        this.body = Base64.encodeBase64String(body.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 添加响应头
     *
     * @param key   响应头键
     * @param value 响应头键值
     */
    public void addHeader(final String key, final String value) {
        if (Objects.isNull(this.headers)) {
            this.headers = Maps.newHashMap();
        }
        this.headers.put(key, value);
    }

    /**
     * 移除响应头
     *
     * @param key 响应头
     */
    public void removeHeader(@Nonnull final String key) {
        if (Objects.nonNull(this.headers) && !Strings.isNullOrEmpty(key)) {
            this.headers.remove(key);
        }

    }

    /**
     * 添加响应头
     *
     * @param headersToAdd 响应头
     */
    public void addHeaders(@Nonnull final Map<String, String> headersToAdd) {
        if (Objects.isNull(this.headers)) {
            this.headers = Maps.newHashMap();
        }
        this.headers.putAll(headersToAdd);
    }
}
