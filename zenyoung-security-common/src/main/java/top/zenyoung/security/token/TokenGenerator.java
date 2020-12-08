package top.zenyoung.security.token;

import top.zenyoung.security.exception.TokenException;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * 令牌-操作接口
 *
 * @author yangyong
 * @version 1.0
 *  2020/3/21 7:33 下午
 **/
public interface TokenGenerator {

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
    String getSignSlat();

    /**
     * 创建令牌
     *
     * @param ticket 令牌票据
     * @return 令牌
     */
    String createToken(@Nonnull final Ticket ticket);

    /**
     * 解析令牌
     *
     * @param token 令牌数据
     * @return 令牌票据
     * @throws TokenException 令牌异常
     */
    Ticket parseToken(@Nonnull final String token) throws TokenException;
}
