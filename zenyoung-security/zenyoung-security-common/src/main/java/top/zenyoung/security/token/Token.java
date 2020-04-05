package top.zenyoung.security.token;

import top.zenyoung.security.exception.TokenException;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * 令牌-操作接口
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/3/21 7:33 下午
 **/
public interface Token {

    /**
     * 获取令牌有效期
     *
     * @return 令牌有效期
     */
    Duration getExpire();

    /**
     * 获取令牌签名盐值
     *
     * @return 令牌签名盐值
     */
    String getTokenSignSlat();

    /**
     * 创建令牌
     *
     * @param ticket 令牌票据
     * @return 令牌
     */
    String createToken(@Nonnull final TokenTicket ticket);

    /**
     * 解析令牌
     *
     * @param json 令牌数据
     * @return 令牌票据
     * @throws TokenException 令牌异常
     */
    TokenTicket parseToken(@Nonnull final String json) throws TokenException;
}
