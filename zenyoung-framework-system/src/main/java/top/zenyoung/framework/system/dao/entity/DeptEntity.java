package top.zenyoung.framework.system.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

/**
 * 部门-数据实体
 *
 * @author young
 */
@Getter
@Setter
@Entity
@ToString
@Accessors(chain = true)
@Table(name = "tbl_sys_dept")
@Where(clause = "status >= 0")
@SQLDelete(sql = "update tbl_sys_dept set status = -1 where id = ?")
public class DeptEntity extends BaseStatusEntity {
    /**
     * 部门代码(排序)
     */
    @Column(nullable = false)
    private Integer code;
    /**
     * 部门名称
     */
    @Column(length = 32, nullable = false, unique = true)
    private String name;
    /**
     * 父部门ID
     */
    @Column
    private Long parentId = 0L;
    /**
     * 祖级列表
     */
    @Column(length = 2048)
    private String ancestors;
    /**
     * 负责人
     */
    @Column(length = 32)
    private String leader;
    /**
     * 联系电话
     */
    @Column(length = 20)
    private String mobile;
    /**
     * 邮箱
     */
    @Column(length = 128)
    private String email;
    /**
     * 关联角色集合(部门下角色集合)
     */
    @ToString.Exclude
    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(name = "tbl_sys_dept_roles",
            joinColumns = @JoinColumn(name = "dept_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private List<RoleEntity> roles;
}
