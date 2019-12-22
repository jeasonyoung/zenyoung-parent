package top.zenyoung.security.spi.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录用户信息
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 8:13 下午
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo implements Serializable {
    /**
     * 用户姓名
     */
    private String name;
    /**
     * 用户昵称
     */
    private String nick;
    /**
     * 用户头像
     */
    private String avatar;
    /**
     * 证件类型
     */
    private String idTypeId;
    /**
     * 证件号
     */
    private String idNumber;
    /**
     * 手机号
     */
    private String mobile;
}
