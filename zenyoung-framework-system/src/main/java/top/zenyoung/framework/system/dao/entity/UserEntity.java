package top.zenyoung.framework.system.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

/**
 * 用户-数据实体
 *
 * @author young
 */
@Getter
@Setter
@Entity
@ToString
@Table(name = "tbl_sys_user")
@Where(clause = "status >= 0")
@SQLDelete(sql = "update tbl_sys_user set status = -1 where id = ?")
public class UserEntity extends BaseStatusEntity {
    /**
     * 用户姓名
     */
    @Column(length = 32)
    private String name;
    /**
     * 用户账号
     */
    @Column(length = 32, nullable = false, unique = true)
    private String account;
    /**
     * 登录密码
     */
    @Column(length = 64)
    private String passwd;
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
     * 所属部门ID
     */
    @Column(nullable = false)
    private Long deptId;
    /**
     * 关联岗位集合
     */
    @ToString.Exclude
    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(name = "tbl_sys_user_posts",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "post_id", referencedColumnName = "id")
    )
    private List<PostEntity> posts;
    /**
     * 关联角色集合
     */
    @ToString.Exclude
    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(name = "tbl_sys_user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private List<RoleEntity> roles;
}
