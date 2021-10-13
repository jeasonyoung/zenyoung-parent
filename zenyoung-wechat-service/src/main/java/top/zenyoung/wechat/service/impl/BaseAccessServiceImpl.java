package top.zenyoung.wechat.service.impl;

import com.google.common.cache.Cache;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import top.zenyoung.common.util.CacheUtils;
import top.zenyoung.okhttp3.WebClient;
import top.zenyoung.okhttp3.WebClientUtils;
import top.zenyoung.wechat.common.GrantType;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Wechat访问-服务接口实现基类
 *
 * @author yangyong
 * @version 1.0
 * date 2020/7/12 7:08 下午
 **/
@Slf4j
abstract class BaseAccessServiceImpl {
    private static final Cache<String, WebClient> WEB_CLIENT_CACHE = CacheUtils.createCache();
    /**
     * Map锁
     */
    protected static final Map<String, Object> LOCKS = Maps.newConcurrentMap();

    /**
     * 获取WebClient
     *
     * @return WebClient
     */
    protected WebClient getWebClient() {
        final String key = WebClient.class.getName();
        synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
            try {
                return CacheUtils.getCacheValue(WEB_CLIENT_CACHE, key, WebClientUtils::getInstance);
            } finally {
                LOCKS.remove(key);
            }
        }
    }

    /**
     * 构建全局令牌地址
     *
     * @param appId     接入ID
     * @param appSecret 接入秘钥
     * @return 令牌地址
     */
    protected String buildAccessTokenUrl(@Nonnull final String appId, @Nonnull final String appSecret) {
        log.debug("buildAccessTokenUrl(appId: {},appSecret: {})...", appId, appSecret);
        Assert.hasText(appId, "'appId'不能为空!");
        Assert.hasText(appSecret, "'appSecret'不能为空!");
        //调用地址URL
        final String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=%1$s&appid=%2$s&secret=%3$s";
        final GrantType grantType = GrantType.ClientCredential;
        //格式化url
        return String.format(url, grantType.getTitle(), appId, appSecret);
    }

    /**
     * 构建Web令牌地址
     *
     * @param appId     接入ID
     * @param appSecret 接入秘钥
     * @param code      授权码
     * @return Web令牌地址
     */
    protected String buildWebAccessTokeUrl(@Nonnull final String appId, @Nonnull final String appSecret, @Nonnull final String code) {
        log.debug("buildWebAccessTokeUrl(appId: {},appSecret: {},code: {})...", appId, appSecret, code);
        Assert.hasText(appId, "'appId'不能为空!");
        Assert.hasText(appSecret, "'appSecret'不能为空!");
        Assert.hasText(code, "'code'不能为空!");
        //调用地址URL
        final String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%1$s&secret=%2$s&code=%3$s&grant_type=%4$s";
        final GrantType grantType = GrantType.AuthorizationCode;
        //格式化url
        return String.format(url, appId, appSecret, code, grantType.getTitle());
    }

    /**
     * 构建用户信息地址
     *
     * @param webAccessToken Web授权令牌
     * @param openId         OpenID
     * @return 用户信息地址
     */
    protected String buildUserInfoUrl(@Nonnull final String webAccessToken, @Nonnull final String openId) {
        log.debug("buildUserInfoUrl(webAccessToken: {},openId: {})..", webAccessToken, openId);
        Assert.hasText(webAccessToken, "'webAccessToken'不能为空!");
        Assert.hasText(openId, "'openId'不能为空!");
        final String url = "https://api.weixin.qq.com/sns/userinfo?access_token=%1$s&openid=%2$s&lang=zh_CN";
        //格式化url
        return String.format(url, webAccessToken, openId);
    }

    /**
     * 构建Jsapi签名票据URL
     *
     * @param accessToken 全局令牌
     * @return Jsapi签名票据URL
     */
    protected String buildJsapiTicketUrl(@Nonnull final String accessToken) {
        log.debug("buildJsapiTicketUrl(accessToken: {})...", accessToken);
        Assert.hasText(accessToken, "'accessToken'不能为空!");
        final String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi";
        //格式化url
        return String.format(url, accessToken);
    }
}
