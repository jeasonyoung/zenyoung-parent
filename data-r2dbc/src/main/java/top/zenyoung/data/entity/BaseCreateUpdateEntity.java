package top.zenyoung.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import top.zenyoung.data.annotations.MappedSuperclass;

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
    private String updatedBy;
    /**
     * 更新时间
     */
    @LastModifiedDate
    private Date updatedAt;
}
