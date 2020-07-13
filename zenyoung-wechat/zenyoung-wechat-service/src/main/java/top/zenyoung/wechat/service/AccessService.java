package top.zenyoung.wechat.service;

import top.zenyoung.wechat.common.AccessToken;
import top.zenyoung.wechat.common.UserInfo;
import top.zenyoung.wechat.common.WebAccessToken;
import top.zenyoung.wechat.common.WebScope;

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
}
