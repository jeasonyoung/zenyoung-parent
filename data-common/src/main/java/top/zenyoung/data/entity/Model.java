package top.zenyoung.data.entity;

import org.springframework.data.domain.Persistable;

import java.io.Serializable;

/**
 * 数据实体接口
 *
 * @author young
 */
public interface Model<K extends Serializable> extends Persistable<K>, Serializable {
    /**
     * 获取主键ID
     *
     * @return 主键ID
     */
    K getId();

    /**
     * 设置主键ID
     *
     * @param id 主键ID
     */
    void setId(final K id);
}
