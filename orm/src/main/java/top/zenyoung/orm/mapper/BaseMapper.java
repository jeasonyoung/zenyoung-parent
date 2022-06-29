package top.zenyoung.orm.mapper;

import top.zenyoung.orm.model.BasePO;

import java.io.Serializable;

/**
 * Mapper接口
 *
 * @author young
 */
public interface BaseMapper<PO extends BasePO<ID>, ID extends Serializable> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<PO> {
    
}
