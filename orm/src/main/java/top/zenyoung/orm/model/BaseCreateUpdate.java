package top.zenyoung.orm.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 数据实体基类(创建、更新字段)
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseCreateUpdate extends BaseCreate {
    /**
     * 更新者
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private Date updateAt;
}
