package com.huawei.services.runtime.entity.dis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 消息数据
 *
 * @author young
 */
@Data
public class DISMessage {
    @JsonProperty("next_patition_cursor")
    private String nextPatitionCursor;
    private DISRecord[] records;
    private String millisBehindLatest;
}
