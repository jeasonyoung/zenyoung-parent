package top.zenyoung.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据实体基类(创建字段)
 *
 * @author young
 */
@Data
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor(force = true)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseCreateEntity<K extends Serializable> implements Model<K> {
    /**
     * 创建者
     */
    @CreatedBy
    @Column(updatable = false)
    private String createdBy;
    /**
     * 创建时间
     */
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    /**
     * 是否新增
     */
    @Transient
    private boolean addNew = true;

    @Override
    public boolean isNew() {
        return addNew;
    }
}
