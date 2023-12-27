package top.zenyoung.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

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
    @LastModifiedBy
    @Column(insertable = false)
    private String updatedBy;
    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
}
