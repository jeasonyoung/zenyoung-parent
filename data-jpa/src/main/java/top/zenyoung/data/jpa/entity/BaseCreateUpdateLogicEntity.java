package top.zenyoung.data.jpa.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * 数据实体基类(创建、更新字段、逻辑删除标识)
 *
 * @author young
 */
@Data
@MappedSuperclass
@Where(clause = "deletedAt = 0")
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "update #{#entityName} set deletedAt = 1 where id = ?")
public abstract class BaseCreateUpdateLogicEntity extends BaseCreateUpdateEntity {
    /**
     * 逻辑删除标识(0:正常, 1:删除)
     */
    @Column(nullable = false, insertable = false)
    private Integer deletedAt;
}
