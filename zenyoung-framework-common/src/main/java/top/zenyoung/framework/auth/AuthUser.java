package top.zenyoung.framework.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import top.zenyoung.common.model.Status;

/**
 * 认证用户
 *
 * @author young
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuthUser extends UserInfo {

    /**
     * 用户密码
     */
    private String password;
    /**
     * 用户状态
     */
    private Status status;
}
