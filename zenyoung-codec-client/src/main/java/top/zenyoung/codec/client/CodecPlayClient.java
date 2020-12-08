package top.zenyoung.codec.client;

import top.zenyoung.codec.client.vo.PlayTicket;
import top.zenyoung.codec.client.vo.PlayToken;
import top.zenyoung.codec.client.vo.RefreshPlayToken;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 播控-客户端接口
 *
 * @author young
 */
public interface CodecPlayClient {

    /**
     * 获取播放令牌
     *
     * @param vodId   视频ID(上传ID)
     * @param playUrl 播放URL
     * @param channel 播放渠道号(100:PC-WEB,101:iOS,102:Android)
     * @return 响应数据
     */
    @Nonnull
    PlayToken getPlayToken(@Nonnull final String vodId, @Nonnull final String playUrl, @Nonnull final Integer channel);

    /**
     * 刷新播放令牌
     *
     * @param refreshToken 刷新令牌
     * @return 播放令牌
     */
    @Nonnull
    RefreshPlayToken refreshPlayToken(@Nonnull final String refreshToken);

    /**
     * 获取播放URL
     *
     * @param token   播放令牌
     * @param browser 浏览器标识[可空]
     * @return 播放URL
     */
    @Nonnull
    String getPlayUrl(@Nonnull final PlayTicket token, @Nullable final String browser);
}
