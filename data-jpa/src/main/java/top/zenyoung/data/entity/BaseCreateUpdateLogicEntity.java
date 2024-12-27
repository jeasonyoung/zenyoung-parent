package top.zenyoung.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;

/**
 * 数据实体基类(创建、更新字段、逻辑删除标识)
 *
 * @author young
 */
@Data
@MappedSuperclass
@SQLRestriction("deletedAt = 0")
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "update #{#entityName} set deletedAt = ? where id = ?")
public abstract class BaseCreateUpdateLogicEntity<K extends Serializable> extends BaseCreateUpdateEntity<K> {
    /**
     * 逻辑删除标识(0:正常, >0:删除)
     */
    @Column(nullable = false, insertable = false)
    private Long deletedAt;
}
