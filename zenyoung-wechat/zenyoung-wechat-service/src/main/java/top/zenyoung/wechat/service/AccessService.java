package top.zenyoung.wechat.service;

import top.zenyoung.wechat.common.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wechat访问-服务接口
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/7/12 7:04 下午
 **/
public interface AccessService {

    /**
     * 获取全局访问令牌
     *
     * @param appId     AppID
     * @param appSecret AppSecret
     * @return 访问令牌
     */
    AccessToken getAccessToken(@Nonnull final String appId, @Nonnull final String appSecret);

    /**
     * 构建Web授权URL
     *
     * @param appId       AppID
     * @param redirectUri 回调链接地址
     * @param scope       授权作用域
     * @param state       state参数(a-zA-Z0-9的参数值，最多128字节)
     * @return Web授权URL
     */
    String buildWebAuthorizeUrl(@Nonnull final String appId, @Nonnull final String redirectUri, @Nonnull final WebScope scope, @Nullable final String state);

    /**
     * 通过code换取网页授权访问令牌
     *
     * @param appId     AppID
     * @param appSecret AppSecret
     * @param code      授权码
     * @return 网页授权访问令牌
     */
    WebAccessToken getWebAccessToken(@Nonnull final String appId, @Nonnull final String appSecret, @Nonnull final String code);

    /**
     * 获取微信用户信息
     *
     * @param token 网页授权访问令牌
     * @return 微信用户信息
     */
    UserInfo getUserInfo(@Nonnull final WebAccessToken token);

    /**
     * 获取Jsapi签名票据
     *
     * @param accessToken 全局访问令牌
     * @param appId       AppID
     * @return Jsapi签名票据
     */
    JsTicket getJsApiTicket(@Nonnull final AccessToken accessToken, @Nonnull final String appId);

    /**
     * 创建JsApi签名处理
     *
     * @param jsTicket  Jsapi签名票据
     * @param url       当前网页的URL
     * @param nonce     随机字符串
     * @param timestamp 时间戳
     * @return JsApi签名
     */
    String createJsApiSignature(@Nonnull final JsTicket jsTicket, @Nonnull final String url, @Nonnull final String nonce, @Nonnull final Long timestamp);
}
