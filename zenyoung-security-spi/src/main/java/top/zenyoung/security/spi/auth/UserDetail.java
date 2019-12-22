package top.zenyoung.security.spi.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 登录用户信息
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 7:53 下午
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetail implements Serializable {
    /**
     * 用户类型
     */
    private Integer type;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 用户账号
     */
    private String account;
    /**
     * 用户密码
     */
    private String password;
    /**
     * 用户角色集合
     */
    private List<String> roles;
    /**
     * 用户是否启用
     */
    private boolean enabled;
    /**
     * 所属组织ID
     */
    private String orgId;
}
