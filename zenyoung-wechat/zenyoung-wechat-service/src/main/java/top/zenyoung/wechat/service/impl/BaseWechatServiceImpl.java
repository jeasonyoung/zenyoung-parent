package top.zenyoung.wechat.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import top.zenyoung.common.util.CacheUtils;
import top.zenyoung.common.util.JsonUtils;
import top.zenyoung.webclient.WebClient;
import top.zenyoung.wechat.common.AccessToken;
import top.zenyoung.wechat.common.UserInfo;
import top.zenyoung.wechat.common.WebAccessToken;
import top.zenyoung.wechat.common.WebScope;
import top.zenyoung.wechat.service.AccessService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * 微信服务基类
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/7/12 7:18 下午
 **/
@Slf4j
public abstract class BaseWechatServiceImpl extends BaseAccessServiceImpl implements AccessService {
    private static final long TOKEN_EXPIRE_DISTANCE = 10 * 60 * 1000;
    private static final Cache<String, AccessTokenCache> ACCESS_TOKEN_CACHE = CacheUtils.createCache(100, 3600, TimeUnit.SECONDS);

    /**
     * 获取ObjectMapper
     *
     * @return ObjectMapper
     */
    @Nonnull
    protected abstract ObjectMapper getObjectMapper();

    /**
     * 从缓存中加载令牌缓存数据
     *
     * @param appId 接入ID
     * @return 令牌缓存数据
     */
    protected abstract AccessTokenCache getAccessTokenCache(@Nonnull final String appId);

    /**
     * 保存令牌缓存数据
     *
     * @param appId 接入ID
     * @param data  令牌缓存数据
     */
    protected abstract void saveAccessTokenCache(@Nonnull final String appId, @Nonnull final AccessTokenCache data);

    @Override
    public AccessToken getAccessToken(@Nonnull final String appId, @Nonnull final String appSecret) {
        log.debug("getAccessToken(appId: {},appSecret: {})...", appId, appSecret);
        Assert.hasText(appId, "'appId'不能为空!");
        Assert.hasText(appSecret, "'appSecret'不能为空!");
        final String key = "access-token:" + appId + "_" + appSecret;
        synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
            try {
                //加载本地缓存
                final AccessTokenCache tokenCache = CacheUtils.getCacheValue(ACCESS_TOKEN_CACHE, key, () -> {
                    //从缓存中加载数据
                    AccessTokenCache cache = getAccessTokenCache(appId);
                    if (cache == null || (System.currentTimeMillis() - cache.getCreateTimeStamp()) < TOKEN_EXPIRE_DISTANCE) {
                        //获取WebClient
                        final WebClient webClient = getWebClient();
                        if (webClient != null) {
                            final String url = buildAccessTokenUrl(appId, appSecret);
                            final String json = webClient.sendRequest("GET", url, null, () -> null, resp -> resp);
                            //数据解析
                            final AccessToken token = JsonUtils.fromJson(getObjectMapper(), json, AccessToken.class);
                            if (token == null) {
                                throw new RuntimeException(json);
                            }
                            cache = AccessTokenCache.of(token, System.currentTimeMillis());
                            //缓存数据
                            saveAccessTokenCache(appId, cache);
                        }
                    }
                    return cache;
                });
                //检查是否即将过期
                if (tokenCache != null && (System.currentTimeMillis() - tokenCache.getCreateTimeStamp()) < TOKEN_EXPIRE_DISTANCE) {
                    //本地缓存过期处理
                    ACCESS_TOKEN_CACHE.invalidate(key);
                }
                return tokenCache == null ? null : tokenCache.getAccessToken();
            } finally {
                LOCKS.remove(key);
            }
        }
    }

    @Override
    public String buildWebAuthorizeUrl(@Nonnull final String appId, @Nonnull final String redirectUri, @Nonnull final WebScope scope, @Nullable final String state) {
        log.debug("buildWebAuthorizeUrl(appId: {},redirectUri: {},scope: {},state: {})...", appId, redirectUri, scope, state);
        Assert.hasText(appId, "'appId'不能为空!");
        Assert.hasText(redirectUri, "'redirectUri'不能为空!");
        final String key = "web-authorize-url:" + appId;
        synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
            try {
                final StringBuilder builder = new StringBuilder("https://open.weixin.qq.com/connect/oauth2/authorize");
                //接入ID
                builder.append("?").append("appid").append("=").append(appId)
                        //回调链接地址
                        .append("&").append("redirect_uri").append("=").append(redirectUri)
                        .append("&").append("response_type").append("=").append("code")
                        //授权作用域
                        .append("&").append("scope").append("=").append(scope.getTitle());
                //重定向后会带上state参数
                if (!Strings.isNullOrEmpty(state)) {
                    builder.append("&").append("state").append("=").append(state);
                }
                //页面302重定向
                builder.append("#wechat_redirect");
                return builder.toString();
            } finally {
                LOCKS.remove(key);
            }
        }
    }

    @SneakyThrows
    @Override
    public WebAccessToken getWebAccessToken(@Nonnull final String appId, @Nonnull final String appSecret, @Nonnull final String code) {
        log.debug("getWebAccessToken(appId: {},appSecret: {},code: {})...", appId, appSecret, code);
        Assert.hasText(appId, "'appId'不能为空!");
        Assert.hasText(appSecret, "'appSecret'不能为空!");
        Assert.hasText(code, "'code'不能为空!");
        final String key = "web-access-token:" + appId;
        synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
            try {
                //获取WebClient
                final WebClient webClient = getWebClient();
                Assert.notNull(webClient, "'webClient'不能为null!");
                //构建访问URL
                final String url = buildWebAccessTokeUrl(appId, appSecret, code);
                final String json = webClient.sendRequest("GET", url, null, () -> null, resp -> resp);
                //数据解析
                final WebAccessToken token = JsonUtils.fromJson(getObjectMapper(), json, WebAccessToken.class);
                if (token == null) {
                    throw new RuntimeException(json);
                }
                return token;
            } finally {
                LOCKS.remove(key);
            }
        }
    }

    @SneakyThrows
    @Override
    public UserInfo getUserInfo(@Nonnull final WebAccessToken webAccessToken) {
        log.debug("getUserInfo(webAccessToken: {})...", webAccessToken);
        Assert.hasText(webAccessToken.getToken(), "'webAccessToken.token'不能为空!");
        Assert.hasText(webAccessToken.getOpenId(), "'webAccessToken.openId'不能为空!");
        final String key = "userInfo:" + webAccessToken.getToken();
        synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
            try {
                //获取WebClient
                final WebClient webClient = getWebClient();
                Assert.notNull(webClient, "'webClient'不能为null!");
                //构建访问URL
                final String url = buildUserInfoUrl(webAccessToken.getToken(), webAccessToken.getOpenId());
                final String json = webClient.sendRequest("GET", url, null, () -> null, resp -> resp);
                //数据解析
                final UserInfo userInfo = JsonUtils.fromJson(getObjectMapper(), json, UserInfo.class);
                if (userInfo == null) {
                    throw new RuntimeException(json);
                }
                return userInfo;
            } finally {
                LOCKS.remove(key);
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    protected static class AccessTokenCache extends AccessToken {
        /**
         * 创建时间戳
         */
        private Long createTimeStamp;

        public AccessToken getAccessToken() {
            return new AccessToken(this.getToken(), this.getExpiresIn());
        }

        public static AccessTokenCache of(@Nonnull final AccessToken accessToken, @Nullable final Long createTimeStamp) {
            final AccessTokenCache data = new AccessTokenCache();
            //令牌
            data.setToken(accessToken.getToken());
            //有效期
            data.setExpiresIn(accessToken.getExpiresIn());
            //创建时间戳
            data.setCreateTimeStamp(createTimeStamp == null || createTimeStamp <= 0 ? System.currentTimeMillis() : createTimeStamp);
            return data;
        }
    }
}
