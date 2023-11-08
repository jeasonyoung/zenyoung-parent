package top.zenyoung.boot.annotation;

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
    DICT,
    /**
     * 上传
     */
    DOWNLOAD,
    /**
     * 业务数据
     */
    BIZ,
    /**
     * 日期(yyyy-MM-dd)
     */
    DATE,
    /**
     * 忽略
     */
    IGNORE
}
