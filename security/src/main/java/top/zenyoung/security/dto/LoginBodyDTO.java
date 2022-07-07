package top.zenyoung.security.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录用户请求数据
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/20 6:10 下午
 **/
@Data
public class LoginBodyDTO implements Serializable {
    /**
     * 登录账号
     */
    private String account;
    /**
     * 登录密码
     */
    private String passwd;
    /**
     * 用户设备标识
     */
    private String device;
    /**
     * 验证码ID
     */
    private Long verifyId;
    /**
     * 验证码
     */
    private String verifyCode;
}
