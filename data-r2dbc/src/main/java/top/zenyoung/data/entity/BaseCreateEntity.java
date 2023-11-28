package top.zenyoung.data.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import top.zenyoung.data.annotations.MappedSuperclass;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据实体基类(创建字段)
 *
 * @author young
 */
@Data
@MappedSuperclass
public abstract class BaseCreateEntity<K extends Serializable> implements Model<K> {
    /**
     * 创建者
     */
    @CreatedBy
    private String createdBy;
    /**
     * 创建时间
     */
    @CreatedDate
    private Date createdAt;
}