package top.zenyoung.codec.client;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import top.zenyoung.codec.client.vo.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 播控-客户端接口实现
 *
 * @author young
 */
@Slf4j
public class CodecPlayClientDefault extends BaseCodecClientDefault implements CodecPlayClient {
    private static final String PLAY_TOKEN_URL = "/play/token";
    private static final String PLAY_REFRESH_TOKEN_URL = "/play/token/refresh";
    private static final String PLAY_URL = "/play.m3u8";

    private CodecPlayClientDefault(@Nonnull final String host, @Nonnull final String account, @Nonnull final String passwd) {
        super(host, account, passwd);
    }

    /**
     * 获取客户端实例
     *
     * @param host    服务器地址
     * @param account 接入账号
     * @param passwd  接入秘钥
     * @return 客户端实例
     */
    public static CodecPlayClientDefault getInstance(@Nonnull final String host, @Nonnull final String account, @Nonnull final String passwd) {
        log.debug("getInstance(host: {},account: {},passwd: {})...", host, account, passwd);
        return new CodecPlayClientDefault(host, account, passwd);
    }

    @Nonnull
    @Override
    public PlayToken getPlayToken(@Nonnull final String vodId, @Nonnull final String playUrl, @Nonnull final Integer channel) {
        log.debug("getPlayToken(vodId: {},playUrl: {},channel: {})...", vodId, playUrl, channel);
        Assert.hasText(vodId, "'vodId'不能为空!");
        Assert.hasText(playUrl, "'playUrl'不能为空!");
        Assert.isTrue(channel >= 0, "'channel'必须大于0!");
        //初始化请求数据
        final PlayTokenReq req = new PlayTokenReq();
        //视频ID(上传ID)
        req.setVodId(vodId);
        //播放URL
        req.setPlayUrl(playUrl);
        //播放渠道号
        req.setChannel(channel);
        //处理请求
        final PlayTokenResp resp = post(PLAY_TOKEN_URL, req, PlayTokenResp.class);
        //返回数据
        return resp.getData();
    }

    @Nonnull
    @Override
    public RefreshPlayToken refreshPlayToken(@Nonnull final String refreshToken) {
        log.debug("refreshPlayToken(refreshToken: {})...", refreshToken);
        Assert.hasText(refreshToken, "'refreshToken'不能为空!");
        final RefreshPlayTokenReq req = new RefreshPlayTokenReq();
        //刷新令牌
        req.setRefreshToken(refreshToken);
        //处理请求
        final RefreshPlayTokenResp resp = post(PLAY_REFRESH_TOKEN_URL, req, RefreshPlayTokenResp.class);
        //返回数据
        return resp.getData();
    }

    @Nonnull
    @Override
    public String getPlayUrl(@Nonnull final PlayTicket token, @Nullable final String browser) {
        log.debug("getPlayUrl(token: {},browser: {})...", token, browser);
        //检查令牌是否有效
        Assert.isTrue(token.getExpire() > System.currentTimeMillis(), "'token'已过期!");
        Assert.hasText(token.getToken(), "'token'不能为空!");
        //参数处理
        final Map<String, Serializable> params = Maps.newLinkedHashMap();
        //播放令牌
        params.put("token", token.getToken());
        //检查浏览器标识
        if (!Strings.isNullOrEmpty(browser)) {
            params.put("browser", browser);
        }
        //参数签名处理
        params.put("sign", buildSignHandler(params));
        //拼接处理
        return buildUrl(PLAY_URL) + "?"
                + (Joiner.on("&").join(params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.toList()))
        );
    }
}
