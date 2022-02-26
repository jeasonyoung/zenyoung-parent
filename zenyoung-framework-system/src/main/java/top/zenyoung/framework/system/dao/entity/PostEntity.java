package top.zenyoung.framework.system.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

/**
 * 岗位-数据实体
 *
 * @author young
 */
@Getter
@Setter
@Entity
@ToString
@Table(name = "tbl_sys_post")
@Where(clause = "status >= 0")
@SQLDelete(sql = "update tbl_sys_post set status = -1 where id = ?")
public class PostEntity extends BaseStatusEntity {
    /**
     * 岗位编码
     */
    @Column(length = 32, nullable = false, unique = true)
    private String code;
    /**
     * 岗位名称
     */
    @Column(length = 32)
    private String name;
    /**
     * 所属部门ID
     */
    @Column(nullable = false)
    private Long deptId;

    /**
     * 关联用户集合
     */
    @ToString.Exclude
    @ManyToMany(mappedBy = "posts")
    private List<UserEntity> users;

    /**
     * 关联角色集合
     */
    @ToString.Exclude
    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(name = "tbl_sys_post_roles",
            joinColumns = @JoinColumn(name = "post_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private List<RoleEntity> roles;
}
