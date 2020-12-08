package top.zenyoung.codec.client.vo;

import lombok.Data;

/**
 * 播放刷新令牌
 *
 * @author young
 */
@Data
public class RefreshPlayToken implements PlayTicket {
    /**
     * 播放令牌(base64格式,有效期5分钟)
     */
    private String token;
    /**
     * 播放令牌到期时间戳(过期时间:毫秒)
     */
    private Long expire;
}
