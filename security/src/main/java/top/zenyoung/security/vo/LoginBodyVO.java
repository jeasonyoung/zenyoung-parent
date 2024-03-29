package top.zenyoung.security.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 登录用户响应数据
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/20 6:38 下午
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginBodyVO implements Serializable {
    /**
     * 访问令牌
     */
    private String accessToken;
    /**
     * 刷新令牌
     */
    private String refershToken;
    /**
     * 用户信息
     */
    private Map<String, Object> user;
}
