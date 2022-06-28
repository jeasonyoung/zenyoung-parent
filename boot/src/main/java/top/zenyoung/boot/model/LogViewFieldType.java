package top.zenyoung.boot.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 日志回显字段类型
 *
 * @author young
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum LogViewFieldType {
    /**
     * 字典
     */
    Dict,
    /**
     * 上传
     */
    Download,
    /**
     * 业务数据
     */
    Biz,
    /**
     * 日期(yyyy-MM-dd)
     */
    Date,
    /**
     * 忽略
     */
    Ignore;
}
