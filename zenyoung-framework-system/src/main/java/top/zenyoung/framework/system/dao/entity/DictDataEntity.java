package top.zenyoung.framework.system.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * 字典数据-数据实体
 *
 * @author young
 */
@Getter
@Setter
@Entity
@ToString
@Table(name = "tbl_sys_dict_data", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"type", "val"})
})
@Where(clause = "status >= 0")
@SQLDelete(sql = "update tbl_sys_dict_data set status = -1 where id = ?")
public class DictDataEntity extends BaseStatusEntity {
    /**
     * 字典代码(排序)
     */
    @Column
    private Integer code;
    /**
     * 字典标签
     */
    @Column
    private String label;
    /**
     * 字典键值
     */
    @Column(name = "val")
    private String value;
    /**
     * 是否默认(0:否,1:是)
     */
    @Column
    private Integer isDefault = 0;
    /**
     * 字典类型
     */
    @Column(length = 128, nullable = false)
    private String type;
    /**
     * 样式属性
     */
    @Column(length = 128)
    private String cssClass;
    /**
     * 表格回显样式
     */
    @Column(length = 128)
    private String listClass;
    /**
     * 备注
     */
    @Column
    private String remark;
}
