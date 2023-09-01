package com.alicom.mns.tools;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * RegionEnum
 *
 * @author aliyun
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RegionEnum {
    HUADONG_1("cn-hangzhou"),
    HUADONG_2("cn-shanghai"),
    HUABEI_2("cn-beijing"),
    HUANAN_1("cn-shenzhen");
    
    private final String regionId;
}
