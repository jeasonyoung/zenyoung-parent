package com.huawei.services.runtime.entity.dis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 触发事件
 *
 * @author young
 */
@Data
public class DISTriggerEvent {
    @JsonProperty("ShardID")
    private String shardId;
    @JsonProperty("Message")
    private DISMessage message;
    @JsonProperty("Tag")
    private String tag;
    @JsonProperty("StreamName")
    private String streamName;
}
