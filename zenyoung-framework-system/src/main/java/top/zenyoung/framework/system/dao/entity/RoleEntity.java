package top.zenyoung.framework.system.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import top.zenyoung.framework.system.dao.converter.DataScopeConverter;
import top.zenyoung.framework.system.model.DataScope;

import javax.persistence.*;
import java.util.List;

/**
 * 角色-数据实体
 *
 * @author young
 */
@Getter
@Setter
@Entity
@ToString
@Table(name = "tbl_sys_role")
@Where(clause = "status >= 0")
@SQLDelete(sql = "update tbl_sys_role set status = -1 where id = ?")
public class RoleEntity extends BaseStatusEntity {
    /**
     * 角色代码(排序)
     */
    @Column
    private Integer code;
    /**
     * 角色名称
     */
    @Column(length = 32, nullable = false, unique = true)
    private String name;
    /**
     * 角色简称
     */
    @Column(length = 32, nullable = false, unique = true)
    private String abbr;
    /**
     * 角色备注
     */
    @Column
    private String remark;
    /**
     * 数据权限范围
     */
    @Column(nullable = false)
    @Convert(converter = DataScopeConverter.class)
    private DataScope scope = DataScope.None;
    /**
     * 关联菜单权限范围(用,分隔)
     */
    @Column
    private String scopeMenus;
    /**
     * 关联部门权限范围(用,分隔)
     */
    @Column
    private String scopeDepts;
    /**
     * 关联岗位集合
     */
    @ToString.Exclude
    @ManyToMany(mappedBy = "roles")
    private List<PostEntity> posts;
    /**
     * 关联部门集合(部门下的角色集合)
     */
    @ToString.Exclude
    @ManyToMany(mappedBy = "roles")
    private List<DeptEntity> depts;
    /**
     * 关联用户集合(角色下用户集合)
     */
    @ToString.Exclude
    @ManyToMany(mappedBy = "roles")
    private List<UserEntity> users;
    /**
     * 关联菜单集合
     */
    @ToString.Exclude
    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(name = "tbl_sys_role_menus",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "menu_id", referencedColumnName = "id")
    )
    private List<MenuEntity> menus;
}
