package top.zenyoung.wechat.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 网页授权访问令牌
 *
 * @author yangyong
 * @version 1.0
 * date 2020/7/12 1:49 下午
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WebAccessToken extends AccessToken {
    private static final String SCOPE_SEP = ",";

    /**
     * 刷新令牌
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * 用户标识
     */
    @JsonProperty("openid")
    private String openId;

    /**
     * 授权作用域
     */
    @JsonProperty("scope")
    private String scope;

    /**
     * 获取授权作用域集合
     *
     * @return 授权作用域集合
     */
    public List<WebScope> getScopes() {
        if (!Strings.isNullOrEmpty(scope)) {
            return StreamSupport.stream(Splitter.on(SCOPE_SEP).trimResults().split(SCOPE_SEP).spliterator(), false)
                    .map(scope -> {
                        if (!Strings.isNullOrEmpty(scope)) {
                            return WebScope.ofTitle(scope);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return null;
    }

    private WebAccessToken(@Nonnull final AccessToken base) {
        this.setToken(base.getToken());
        this.setExpiresIn(base.getExpiresIn());
    }

    public static WebAccessToken of(@Nonnull final String accessToken, @Nullable final Integer expiresIn,
                                    @Nonnull final String refreshToken, @Nonnull final String openId,
                                    @Nonnull final WebScope... scopes) {
        final WebAccessToken webAccessToken = new WebAccessToken(of(accessToken, expiresIn));
        //刷新令牌
        webAccessToken.setRefreshToken(refreshToken);
        //用户标识
        webAccessToken.setOpenId(openId);
        //授权作用域
        if (scopes.length > 0) {
            webAccessToken.setScope(Joiner.on(SCOPE_SEP)
                    .join(Stream.of(scopes)
                            .map(WebScope::getTitle)
                            .distinct()
                            .collect(Collectors.toList())
                    ));
        }
        return webAccessToken;
    }
}
