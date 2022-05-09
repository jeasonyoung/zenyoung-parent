package top.zenyoung.framework.auth;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.security.model.LoginReqBody;

/**
 * 用户登录-数据DTO
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginDTO extends LoginReqBody {
    /**
     * 验证码ID
     */
    private Long verifyId;
    /**
     * 验证码
     */
    private String verifyCode;
}
