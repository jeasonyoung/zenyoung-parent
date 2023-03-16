package com.huawei.services.runtime.entity.apig;

import lombok.Data;

/**
 * 请求上下文
 *
 * @author young
 */
@Data
public class APIGRequestContext {
    /**
     * API ID
     */
    private String apiId;
    /**
     * 请求ID
     */
    private String requestId;
    /**
     * 阶段
     */
    private String stage;
    /**
     * 源IP
     */
    private String sourceIp;
}
