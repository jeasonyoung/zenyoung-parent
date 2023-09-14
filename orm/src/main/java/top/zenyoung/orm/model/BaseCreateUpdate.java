package top.zenyoung.orm.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据实体基类(创建、更新字段)
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseCreateUpdate<K extends Serializable> extends BaseCreate<K> {
    /**
     * 更新者
     */
    private String updatedBy;
    /**
     * 更新时间
     */
    private Date updatedAt;
}
