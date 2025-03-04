package top.zenyoung.data.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据实体基类(创建字段)
 *
 * @author young
 */
@Data
public abstract class BaseCreate<K extends Serializable> implements Model<K> {
    /**
     * 创建者
     */
    private String createdBy;
    /**
     * 创建时间
     */
    private Date createdAt;
}
