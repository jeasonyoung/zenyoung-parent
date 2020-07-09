package top.zenyoung.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
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
@AllArgsConstructor
public class TokenLiveTime implements Serializable {
    /**
     * 令牌生存期
     */
    private Duration tokenLiveTime;
    /**
     * 刷新令牌生存期
     */
    private Duration refreshTokenLiveTime;

    /**
     * 静态构建令牌生存期
     *
     * @param tokenLiveTime        令牌生存期
     * @param refreshTokenLiveTime 刷新令牌生存期
     * @return 令牌生存期
     */
    public TokenLiveTime of(@Nullable final Duration tokenLiveTime, @Nullable final Duration refreshTokenLiveTime) {
        return new TokenLiveTime(tokenLiveTime, refreshTokenLiveTime);
    }
}
