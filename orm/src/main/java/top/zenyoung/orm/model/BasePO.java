package top.zenyoung.orm.model;

import java.io.Serializable;

/**
 * 数据实体接口
 *
 * @author young
 */
public interface BasePO<ID extends Serializable> {
    /**
     * 获取主键ID
     *
     * @return 主键ID
     */
    ID getId();

    /**
     * 设置主键ID
     *
     * @param id 主键ID
     */
    void setId(final ID id);
}
