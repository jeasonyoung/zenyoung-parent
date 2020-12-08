package top.zenyoung.wechat.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.Assert;
import top.zenyoung.common.util.CacheUtils;
import top.zenyoung.common.util.JsonUtils;
import top.zenyoung.webclient.WebClient;
import top.zenyoung.wechat.common.*;
import top.zenyoung.wechat.service.AccessService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 微信服务基类
 *
 * @author yangyong
 * @version 1.0
 * date 2020/7/12 7:18 下午
 **/
@Slf4j
public abstract class BaseWechatServiceImpl extends BaseAccessServiceImpl implements AccessService {
    private static final Duration EXPIRE = Duration.ofSeconds(3600);
    private static final long EXPIRE_DISTANCE = 10 * 60 * 1000;

    private static final Cache<String, AccessTokenCache> ACCESS_TOKEN_CACHE = CacheUtils.createCache(100, (int) EXPIRE.getSeconds(), TimeUnit.SECONDS);
    private static final Cache<String, JsTicketCache> TICKET_CACHE = CacheUtils.createCache(100, (int) EXPIRE.getSeconds(), TimeUnit.SECONDS);

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
     * 缓存令牌缓存数据
     *
     * @param appId AppID
     * @param cache 令牌缓存数据
     */
    protected abstract void saveAccessTokenCache(@Nonnull final String appId, @Nonnull final AccessTokenCache cache);

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
                    if (cache == null || (System.currentTimeMillis() - cache.getCreateTimeStamp()) < EXPIRE_DISTANCE) {
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
                if (tokenCache != null && (System.currentTimeMillis() - tokenCache.getCreateTimeStamp()) < EXPIRE_DISTANCE) {
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

    /**
     * 从缓存中加载Jsapi票据缓存数据
     *
     * @param appId AppID
     * @return 票据缓存数据
     */
    protected abstract JsTicketCache getJsApiTicketCache(@Nonnull final String appId);

    /**
     * 缓存Jsapi票据缓存数据
     *
     * @param appId AppID
     * @param cache 票据缓存数据
     */
    protected abstract void saveJsApiTicketCache(@Nonnull final String appId, @Nonnull final JsTicketCache cache);

    @Override
    public JsTicket getJsApiTicket(@Nonnull final AccessToken accessToken, @Nonnull final String appId) {
        log.debug("getJsApiTicket(accessToken: {},appId: {})..", accessToken, appId);
        Assert.hasText(appId, "'appId'不能为空!");
        final String key = "jsapi-ticket:" + appId;
        synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
            try {
                final JsTicketCache ticketCache = CacheUtils.getCacheValue(TICKET_CACHE, key, () -> {
                    //从缓存中加载数据
                    JsTicketCache cache = getJsApiTicketCache(appId);
                    if (cache == null || (System.currentTimeMillis() - cache.getCreateTimeStamp()) < EXPIRE_DISTANCE) {
                        final String token;
                        if (!Strings.isNullOrEmpty(token = accessToken.getToken())) {
                            //获取WebClient
                            final WebClient webClient = getWebClient();
                            if (webClient != null) {
                                final String url = buildJsapiTicketUrl(token);
                                final String json = webClient.sendRequest("GET", url, null, () -> null, resp -> resp);
                                //数据解析
                                final JsTicket jsTicket = JsonUtils.fromJson(getObjectMapper(), json, JsTicket.class);
                                if (jsTicket == null || Strings.isNullOrEmpty(jsTicket.getTicket())) {
                                    throw new RuntimeException(json);
                                }
                                cache = JsTicketCache.of(jsTicket, System.currentTimeMillis());
                                //缓存数据
                                saveJsApiTicketCache(appId, cache);
                            }
                        }
                    }
                    return cache;
                });
                //检查是否即将过期
                if (ticketCache != null && (System.currentTimeMillis() - ticketCache.getCreateTimeStamp()) < EXPIRE_DISTANCE) {
                    //本地缓存过期处理
                    TICKET_CACHE.invalidate(key);
                }
                return ticketCache == null ? null : ticketCache.getJsTicket();
            } finally {
                LOCKS.remove(key);
            }
        }
    }

    @Override
    public String createJsApiSignature(@Nonnull final JsTicket jsTicket, @Nonnull final String url,
                                       @Nonnull final String nonce, @Nonnull final Long timestamp) {
        log.debug("createJsApiSignature(jsTicket: {},url: {},nonce: {},timestamp: {})...", jsTicket, url, nonce, timestamp);
        Assert.hasText(url, "'url'不能为空!");
        Assert.hasText(nonce, "'nonce'不能为空!");
        final String ticket;
        Assert.hasText(ticket = jsTicket.getTicket(), "'ticket'不能为空!");
        final String key = "js-api-signature:" + DigestUtils.sha1Hex(ticket);
        synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
            try {
                //参数集合
                final Map<String, Serializable> params = new HashMap<String, Serializable>(4) {
                    {
                        //随机字符串
                        put("noncestr", nonce);
                        //jsapi_ticket
                        put("jsapi_ticket", ticket);
                        //时间戳
                        put("timestamp", timestamp);
                        //当前网页的URL
                        final int idx = url.lastIndexOf("#");
                        put("url", idx > 0 ? url.substring(0, idx) : url);
                    }
                };
                //排序处理
                final String preSign = params.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining("&"));
                return DigestUtils.sha1Hex(preSign);
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
            return AccessToken.of(this.getToken(), this.getExpiresIn());
        }

        public static AccessTokenCache of(@Nonnull final AccessToken accessToken, @Nullable final Long createTimeStamp) {
            final AccessTokenCache cache = new AccessTokenCache();
            //令牌
            cache.setToken(accessToken.getToken());
            //有效期
            cache.setExpiresIn(accessToken.getExpiresIn());
            //创建时间戳
            cache.setCreateTimeStamp(createTimeStamp == null || createTimeStamp <= 0 ? System.currentTimeMillis() : createTimeStamp);
            //返回
            return cache;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    protected static class JsTicketCache extends JsTicket {
        /**
         * 创建时间戳
         */
        private Long createTimeStamp;

        public JsTicket getJsTicket() {
            return JsTicket.of(getTicket(), getExpiresIn());
        }

        public static JsTicketCache of(@Nonnull final JsTicket ticket, @Nullable final Long createTimeStamp) {
            final JsTicketCache cache = new JsTicketCache();
            //临时票据
            cache.setTicket(ticket.getTicket());
            //有效期(7200s)
            cache.setExpiresIn(ticket.getExpiresIn());
            //创建时间戳
            cache.setCreateTimeStamp(createTimeStamp == null || createTimeStamp <= 0 ? System.currentTimeMillis() : createTimeStamp);
            //返回
            return cache;
        }
    }
}
