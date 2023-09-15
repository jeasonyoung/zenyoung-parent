package top.zenyoung.jpa.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Date;

/**
 * 数据实体基类(创建、更新字段)
 *
 * @author young
 */
@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
public abstract class BaseCreateUpdateEntity<K extends Serializable> extends BaseCreateEntity<K> {
    /**
     * 更新者
     */
    @Column(insertable = false)
    private String updatedBy;
    /**
     * 更新时间
     */
    @Column(insertable = false)
    private Date updatedAt;
}
