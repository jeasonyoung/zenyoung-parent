package top.zenyoung.orm.model;

import com.baomidou.mybatisplus.annotation.TableLogic;
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
public abstract class BaseCreateUpdateLogic<ID extends Serializable> extends BaseCreateUpdate {
    /**
     * 逻辑删除标识(0:正常, ID:删除)
     */
    @TableLogic(value = "0", delval = "id")
    private ID logicDel;
}
