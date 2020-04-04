package top.zenyoung.security.spi.auth;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录-请求报文体
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 8:11 下午
 **/
@Data
public class ReqLoginBody implements Serializable {
    /**
     * 用户类型
     */
    private Integer type;
    /**
     * 用户账号
     */
    private String account;
    /**
     * 登录密码
     */
    private String passwd;
}
