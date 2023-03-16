package com.huawei.services.runtime.entity.dms;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DMS触发事件
 *
 * @author young
 */
@Data
public class DMSTriggerEvent {
    @JsonProperty("queue_id")
    private String queueId;
    private String region;
    @JsonProperty("event_type")
    private String eventType;
    @JsonProperty("consumer_group_id")
    private String consumerGroupId;
    private DMSMessage[] messages;
}
