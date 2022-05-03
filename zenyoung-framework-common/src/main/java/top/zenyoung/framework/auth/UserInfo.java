package top.zenyoung.framework.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 用户信息
 *
 * @author young
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo implements Serializable {
    /**
     * 用户ID
     */
    private Long id;
    /**
     * 用户账号
     */
    private String account;
    /**
     * 用户姓名
     */
    private String name;
    /**
     * 用户昵称
     */
    private String nick;
    /**
     * 头像URL
     */
    private String avatar;
    /**
     * 角色集合
     */
    private List<String> roles;
}
