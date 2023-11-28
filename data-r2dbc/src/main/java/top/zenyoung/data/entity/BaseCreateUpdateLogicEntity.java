package top.zenyoung.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.data.annotations.MappedSuperclass;

import java.io.Serializable;

/**
 * 数据实体基类(创建、更新字段、逻辑删除标识)
 *
 * @author young
 */
@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
public abstract class BaseCreateUpdateLogicEntity<K extends Serializable> extends BaseCreateUpdateEntity<K> {
    /**
     * 逻辑删除标识(0:正常, 1:删除)
     */
    private Integer deletedAt;
}
