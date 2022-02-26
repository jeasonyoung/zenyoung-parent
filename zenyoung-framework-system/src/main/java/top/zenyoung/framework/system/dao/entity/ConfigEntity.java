package top.zenyoung.framework.system.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 参数配置-数据实体
 *
 * @author young
 */
@Getter
@Setter
@Entity
@ToString
@Table(name = "tbl_sys_config")
@Where(clause = "status >= 0")
@SQLDelete(sql = "update tbl_sys_config set status = -1 where id = ?")
public class ConfigEntity extends BaseStatusEntity {
    /**
     * 参数名称
     */
    @Column(length = 128, nullable = false)
    private String name;
    /**
     * 参数键名
     */
    @Column(length = 128, nullable = false)
    private String key;
    /**
     * 参数键值
     */
    @Column(length = 512, nullable = false)
    private String val;
    /**
     * 系统内置(0:否,1:是)
     */
    @Column(nullable = false)
    private Integer type = 0;
}
