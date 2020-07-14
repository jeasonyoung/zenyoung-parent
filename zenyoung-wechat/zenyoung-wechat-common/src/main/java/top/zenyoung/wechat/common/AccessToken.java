package top.zenyoung.wechat.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * 授权访问令牌
 *
 * @author yangyong
 * @version 1.0
 * date 2020/7/12 1:00 下午
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken implements Serializable {
    /**
     * 默认有效期
     */
    public static final int DEF_EXPIRED_IN = 7200;

    /**
     * 令牌串
     */
    @JsonProperty("access_token")
    private String token;
    /**
     * 有效期(7200s)
     */
    @JsonProperty("expires_in")
    private Integer expiresIn;

    /**
     * 构建WeChat令牌数据
     *
     * @param token     令牌串
     * @param expiresIn 有效期
     * @return WeChat令牌数据
     */
    public static AccessToken of(@Nonnull final String token, @Nullable final Integer expiresIn) {
        return new AccessToken(token, (expiresIn == null || expiresIn <= 0) ? DEF_EXPIRED_IN : expiresIn);
    }
}
