package com.huawei.services.runtime.entity.dis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

/**
 * Record
 *
 * @author young
 */
@Data
public class DISRecord {
    @JsonProperty("partition_key")
    private String partitionKey;
    private String data;
    @JsonProperty("sequence_number")
    private String sequenceNumber;
    private long timestamp;
    @JsonProperty("timestamp_type")
    private String timestampType;

    /**
     * 获取Raw数据
     *
     * @return Raw数据
     */
    public String getRawData() {
        final byte[] decoded = Base64.decodeBase64(this.data);
        return new String(decoded, StandardCharsets.UTF_8);
    }
}
