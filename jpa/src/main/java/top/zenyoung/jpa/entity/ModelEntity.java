package top.zenyoung.jpa.entity;

import javax.persistence.Id;
import java.io.Serializable;

/**
 * 数据实体接口
 *
 * @author young
 */
public interface ModelEntity<K extends Serializable> extends Serializable {
    /**
     * 获取主键ID
     *
     * @return 主键ID
     */
    @Id
    K getId();

    /**
     * 设置主键ID
     *
     * @param id 主键ID
     */
    void setId(final K id);
}
