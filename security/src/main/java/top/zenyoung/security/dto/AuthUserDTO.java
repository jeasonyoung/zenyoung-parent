package top.zenyoung.security.dto;

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
public class AuthUserDTO extends UserInfoDTO {
    /**
     * 应用代码
     */
    private String appCode;
    /**
     * 用户密码
     */
    private String password;
    /**
     * 用户状态
     */
    private Status status;
}
