package top.zenyoung.jpa.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Date;

/**
 * 数据实体基类(创建字段)
 *
 * @author young
 */
@Data
@MappedSuperclass
public abstract class BaseCreateEntity<K extends Serializable> implements ModelEntity<K> {
    /**
     * 创建者
     */
    @Column(insertable = false)
    private String createdBy;
    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private Date createdAt;
}
