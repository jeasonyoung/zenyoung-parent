package top.zenyoung.security.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 登录用户响应数据
 *
 * @author yangyong
 * @version 1.0
 *  2020/3/20 6:38 下午
 **/
@Data
public class LoginRespBody implements Serializable {
    /**
     * 登录令牌
     */
    private String token;
    /**
     * 刷新令牌
     */
    private String refreshToken;
    /**
     * 用户信息
     */
    private Map<String, Serializable> user;
}
