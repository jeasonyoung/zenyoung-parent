package top.zenyoung.common.model;

import lombok.Data;

import java.io.Serializable;
import java.time.Duration;

/**
 * 令牌生存期
 *
 * @author yangyong
 * @version 1.0
 **/
@Data
public class TokenLiveTime implements Serializable {
    /**
     * 令牌生存期
     */
    private Duration tokenLiveTime;
    /**
     * 刷新令牌生存期
     */
    private Duration refreshTokenLiveTime;
}
