package top.zenyoung.jpa.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * 数据实体基类(创建、更新字段)
 *
 * @author young
 */
@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
public class BaseCreateUpdateEntity extends BaseCreateEntity {
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
