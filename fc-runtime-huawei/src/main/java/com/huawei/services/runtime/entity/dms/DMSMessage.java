package com.huawei.services.runtime.entity.dms;

import lombok.Data;

import java.util.Map;

/**
 * 消息数据
 *
 * @author young
 */
@Data
public class DMSMessage {
    private Object body;
    private Map<String, String> attributes;
}
