package top.zenyoung.framework.runtime.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.common.sequence.Sequence;
import top.zenyoung.security.exception.TokenException;
import top.zenyoung.security.token.Ticket;
import top.zenyoung.security.token.Token;
import top.zenyoung.security.token.TokenService;

import javax.annotation.Nonnull;

/**
 * 令牌服务接口实现
 *
 * @author young
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final IdSequence sequence;

    @Override
    public Token createToken(@Nonnull final Ticket ticket) {

        ///TODO:
        return null;
    }

    @Override
    public String refreshToken(@Nonnull final String refreshToken) {
        return null;
    }

    @Override
    public Ticket parseToken(@Nonnull final String token) throws TokenException {
        return null;
    }

    @Override
    public Ticket parseRefreshToken(@Nonnull final String refreshToken) {
        return null;
    }

    @Override
    public Ticket validToken(@Nonnull final String token) {
        return null;
    }

    @Override
    public void delToken(@Nonnull final String token) {

    }

    @Override
    public String getRefreshToken(@Nonnull final String token) {
        return null;
    }

    @Override
    public String getToken(@Nonnull final String refreshToken) {
        return null;
    }
}
