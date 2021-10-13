package top.zenyoung.generator.domain;

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
    private boolean required;
    /**
     * 是否为主键
     */
    private boolean primaryKey;
    /**
     * 是否为自增列
     */
    private boolean increment;
    /**
     * 是否为时间戳类型
     */
    private boolean timestamp;
}
