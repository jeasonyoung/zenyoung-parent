package top.zenyoung.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;
import top.zenyoung.data.converter.TimestampBooleanConverter;

import java.io.Serializable;

/**
 * 数据实体基类(创建、更新字段、逻辑删除标识)
 *
 * @author young
 */
@Data
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@SoftDelete(columnName = "deleted_at", converter = TimestampBooleanConverter.class)
public abstract class BaseCreateUpdateLogicEntity<K extends Serializable> extends BaseCreateUpdateEntity<K> {
    /**
     * 逻辑删除标识(0:正常, >0:删除)
     */
    @Column(nullable = false, insertable = false, updatable = false)
    private Long deletedAt;
}
