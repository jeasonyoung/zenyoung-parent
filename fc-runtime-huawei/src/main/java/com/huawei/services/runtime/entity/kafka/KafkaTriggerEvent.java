package com.huawei.services.runtime.entity.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Kafka触发事件
 *
 * @author young
 */
@Data
public class KafkaTriggerEvent {
    @JsonProperty("event_version")
    private String eventVersion;
    private String region;
    @JsonProperty("event_time")
    private String eventTime;
    @JsonProperty("trigger_type")
    private String triggerType;
    @JsonProperty("instance_id")
    private String instanceId;
    @JsonProperty("records")
    private KafkaRecord[] records;
}
