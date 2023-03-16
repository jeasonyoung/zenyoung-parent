package com.huawei.services.runtime.entity.obs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Obs对象
 * @author young
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObsObject {
    private int size;
    private String key;
    private String eTag;
    private String versionId;
    private String sequencer;
}
