package top.zenyoung.codec.client.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 播放令牌
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PlayToken extends RefreshPlayToken {
    /**
     * 刷新令牌(有效期30分钟)
     */
    private String refreshToken;
}
