package top.zenyoung.orm.model;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;

/**
 * 逻辑删除基类
 *
 * @author young
 */
@Data
public abstract class BaseLogicDel<ID extends Serializable> implements Serializable {
    /**
     * 逻辑删除标识(0:正常, ID:删除)
     */
    @TableLogic(value = "0", delval = "id")
    private ID logicDel;
}
