package com.huawei.services.runtime.entity.cts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * CTS
 *
 * @author young
 */
@Data
public class CTS {
    private String time;
    private User user;
    private Map<String, String> request;
    private Map<String, String> response;
    private int code;
    @JsonProperty("service_type")
    private String serviceType;
    @JsonProperty("resource_type")
    private String resourceType;
    @JsonProperty("resource_name")
    private String resourceName;
    @JsonProperty("resource_id")
    private String resourceId;
    @JsonProperty("trace_name")
    private String traceName;
    @JsonProperty("trace_type")
    private String traceType;
    @JsonProperty("record_time")
    private String recordTime;
    @JsonProperty("trace_id")
    private String traceId;
    @JsonProperty("trace_status")
    private String traceStatus;
}
