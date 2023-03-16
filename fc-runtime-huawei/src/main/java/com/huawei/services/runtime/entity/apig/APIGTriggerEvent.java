package com.huawei.services.runtime.entity.apig;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * 触发事件
 *
 * @author young
 */
@Data
public class APIGTriggerEvent {
    private boolean isBase64Encoded;
    private String httpMethod;
    private String path;
    private String body;
    private Map<String, String> pathParameters;
    private APIGRequestContext requestContext;
    private Map<String, String> headers;
    private Map<String, Object> queryStringParameters;
    @JsonProperty("user_data")
    private String userData;
}
