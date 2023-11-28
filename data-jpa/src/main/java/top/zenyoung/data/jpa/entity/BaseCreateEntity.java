package top.zenyoung.data.jpa.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 数据实体基类(创建字段)
 *
 * @author young
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseCreateEntity implements Serializable {
    /**
     * 创建者
     */
    @CreatedBy
    @Column(insertable = false)
    private String createdBy;
    /**
     * 创建时间
     */
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdAt;
}
