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
 * 字典类型-数据实体
 *
 * @author young
 */
@Getter
@Setter
@Entity
@ToString
@Table(name = "tbl_sys_dict_type")
@Where(clause = "status >= 0")
@SQLDelete(sql = "update tbl_sys_dict_type set status = -1 where id = ?")
public class DictTypeEntity extends BaseStatusEntity {
    /**
     * 字典名称
     */
    @Column(length = 64, nullable = false)
    private String name;
    /**
     * 字典类型
     */
    @Column(length = 128, nullable = false, unique = true)
    private String type;
    /**
     * 字典备注
     */
    @Column
    private String remark;
}
