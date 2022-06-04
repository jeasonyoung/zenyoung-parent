package top.zenyoung.framework.runtime.service.impl;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import top.zenyoung.framework.enums.ExceptionEnum;
import top.zenyoung.framework.exception.ServiceException;
import top.zenyoung.framework.utils.BeanCacheUtils;
import top.zenyoung.security.exception.TokenException;
import top.zenyoung.security.exception.TokenExpireException;
import top.zenyoung.security.token.Ticket;
import top.zenyoung.security.token.TokenLimitService;
import top.zenyoung.security.token.TokenService;
import top.zenyoung.security.token.TokenVerifyService;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * 令牌校验-服务接口实现
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor
public class TokenVerifyServiceImpl implements TokenVerifyService {
    private final ApplicationContext context;

    private Ticket parseToken(@Nonnull final String accessToken) throws TokenException {
        return BeanCacheUtils.function(context, TokenService.class, bean -> bean.parseToken(accessToken));
    }

    private Ticket parseRefreshToken(@Nonnull final String refreshToken) {
        return BeanCacheUtils.function(context, TokenService.class, bean -> bean.parseRefreshToken(refreshToken));
    }

    private String getRefreshToken(@Nonnull final String accessToken) {
        return BeanCacheUtils.function(context, TokenService.class, bean -> bean.getRefreshToken(accessToken));
    }

    @Override
    public Ticket checkToken(@Nonnull final String token) {
        Ticket ticket;
        try {
            ticket = parseToken(token);
        } catch (TokenExpireException e) {
            log.warn("checkToken(token: {})-exp: {}", token, e.getMessage());
            //令牌过期,获取刷新令牌
            final String refreshToken = getRefreshToken(token);
            if (Strings.isNullOrEmpty(refreshToken)) {
                throw new ServiceException(ExceptionEnum.REFRESH_EXPIRE);
            }
            final Ticket t = checkRefreshToken(refreshToken);
            if (Objects.nonNull(t)) {
                //判断limit是否存在
                judgeLimitExists(t, token);
            }
            //token过期
            throw new ServiceException(ExceptionEnum.TOKEN_EXPIRE);
        } catch (TokenException e) {
            log.warn("checkToken(token: {})-exp: {}", token, e.getMessage());
            //令牌异常
            throw new ServiceException(ExceptionEnum.TOKEN_ERROR);
        }
        return ticket;
    }

    @Override
    public Ticket checkRefreshToken(@Nonnull final String refreshToken) {
        try {
            return parseRefreshToken(refreshToken);
        } catch (Throwable e) {
            log.warn("checkRefreshToken(refreshToken: {})-exp: {}", refreshToken, e.getMessage());
            throw new ServiceException(ExceptionEnum.REFRESH_EXPIRE);
        }
    }

    /**
     * 判断limit是否存在
     */
    private void judgeLimitExists(@Nonnull final Ticket ticket, @Nonnull final String token) {
        BeanCacheUtils.consumer(context, TokenLimitService.class, bean -> {
            //检查是否已被挤出来
            if (!bean.isExists(ticket, token)) {
                throw new ServiceException(ExceptionEnum.LOGIN_OTHER);
            }
        });
    }
}
