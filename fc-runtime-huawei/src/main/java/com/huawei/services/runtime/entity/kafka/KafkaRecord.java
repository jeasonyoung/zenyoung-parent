package com.huawei.services.runtime.entity.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Kafka记录
 *
 * @author young
 */
@Data
public class KafkaRecord {
    private String[] messages;
    @JsonProperty("topic_id")
    private String topicId;
}
