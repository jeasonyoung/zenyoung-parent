package top.zenyoung.framework.generator.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 列数据
 *
 * @author young
 */
@Data
public class Column implements Serializable {
    /**
     * 排序
     */
    private Integer code;
    /**
     * 列名
     */
    private String columnName;
    /**
     * 列描述
     */
    private String columnComment;
    /**
     * 列类型
     */
    private Class<?> columnType;
    /**
     * 是否必须
     */
    private Boolean required;
    /**
     * 是否为主键
     */
    private Boolean primaryKey;
    /**
     * 是否为自增列
     */
    private Boolean increment;
    /**
     * 是否为时间戳类型
     */
    private Boolean timestamp;
}
