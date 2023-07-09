package top.zenyoung.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Duration;

/**
 * 令牌生存期
 *
 * @author yangyong
 * @version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
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
