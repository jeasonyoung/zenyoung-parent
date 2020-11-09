package top.zenyoung.codec.client.vo;

import java.io.Serializable;

/**
 * 令牌票据
 *
 * @author young
 */
public interface PlayTicket extends Serializable {
    /**
     * 获取播放令牌
     *
     * @return 播放令牌
     */
    String getToken();

    /**
     * 获取播放令牌到期时间戳
     *
     * @return 到期时间戳
     */
    Long getExpire();
}
