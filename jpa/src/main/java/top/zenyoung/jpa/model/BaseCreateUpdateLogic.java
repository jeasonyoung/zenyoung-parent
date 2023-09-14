package top.zenyoung.jpa.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 数据实体基类(创建、更新字段、逻辑删除标识)
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseCreateUpdateLogic<K extends Serializable> extends BaseCreateUpdate<K> {
    /**
     * 逻辑删除标识(0:正常, 1:删除)
     */
    private Integer deletedAt;
}
