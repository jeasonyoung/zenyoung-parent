package com.huawei.services.runtime.entity.obs;

/**
 * Obs触发信息
 *
 * @author young
 */
public interface ObsTriggerInfo {
    /**
     * 获取Bucket名称
     *
     * @return Bucket名称
     */
    String getBucketName();

    /**
     * 获取对象键
     *
     * @return 对象键
     */
    String getObjectKey();

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    String getEventName();
}
