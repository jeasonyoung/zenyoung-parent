package top.zenyoung.security.spi.token;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * 令牌票据
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 8:00 下午
 **/
@Getter
@AllArgsConstructor
public class TokenTicket implements Serializable {
    /**
     * 登录令牌
     */
    private String token;
    /**
     * 刷新令牌
     */
    private String refreshToken;
}
