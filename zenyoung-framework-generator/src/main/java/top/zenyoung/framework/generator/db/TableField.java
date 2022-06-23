package top.zenyoung.framework.generator.db;

import lombok.Builder;
import lombok.Data;
import top.zenyoung.framework.generator.type.ColumnType;

import java.io.Serializable;

/**
 * 表字段
 *
 * @author young
 */
@Data
@Builder
public class TableField implements Serializable {
    /**
     * 字段名
     */
    private String name;
    /**
     * 字段描述
     */
    private String comment;
    /**
     * 是否是主键
     */
    private Boolean isPrimaryKey;
    /**
     * 字段数据类型
     */
    private String type;
    /**
     * 字段Java数据类型
     */
    private ColumnType columnType;
    /**
     * 字段Java数据类型
     */
    private String propertyType;
    /**
     * 驼峰式字段名
     */
    private String propertyName;
}
