package top.zenyoung.framework.runtime.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.common.util.JsonUtils;
import top.zenyoung.framework.auth.AuthProperties;
import top.zenyoung.framework.service.RedisEnhancedService;
import top.zenyoung.security.exception.TokenException;
import top.zenyoung.security.exception.TokenExpireException;
import top.zenyoung.security.token.Ticket;
import top.zenyoung.security.token.Token;
import top.zenyoung.security.token.TokenLimitService;
import top.zenyoung.security.token.TokenService;
import top.zenyoung.security.util.JwtUtils;
import top.zenyoung.service.SyncLockService;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 令牌服务接口实现
 *
 * @author young
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final static String REFRESH_TOKEN_PREFIX = "refresh-token:";
    private final static String TOKEN_WITH_REFRESH_PREFIX = "token-with-refresh:";
    private final static String REFRESH_WITH_TOKEN_PREFIX = "refresh-with-token:";
    private final static String SEP = "_";

    private final StringRedisTemplate redisTemplate;
    private final IdSequence sequence;
    private final ObjectMapper objMapper;
    private final AuthProperties authProperties;
    private final RedisEnhancedService enhancedService;
    private final SyncLockService lockService;
    private final TokenLimitService limitService;

    private static String getRefreshTokenKey(@Nonnull final Ticket ticket) {
        final StringBuilder builder = new StringBuilder(REFRESH_TOKEN_PREFIX);
        final String device;
        if (!Strings.isNullOrEmpty(device = ticket.getDevice())) {
            builder.append(device).append(SEP);
        }
        builder.append(ticket.getId());
        return builder.toString();
    }

    private static String getTokenWithRefreshKey(@Nonnull final String refreshToken) {
        return TOKEN_WITH_REFRESH_PREFIX + refreshToken;
    }

    private static String getTicketWithRefreshKey(@Nonnull final String refreshToken) {
        return REFRESH_TOKEN_PREFIX + "ticket" + SEP + refreshToken;
    }

    private static String getRefreshWithTokenKey(@Nonnull final String accessToken) {
        return REFRESH_WITH_TOKEN_PREFIX + accessToken;
    }

    @Override
    public Token createToken(@Nonnull final Ticket ticket) {
        final Long tokenId = sequence.nextId();
        //创建访问令牌
        final String accessToken = JwtUtils.create(tokenId + "", ticket, authProperties.getTokenExpire());
        //检查刷新令牌
        String refreshToken = getRefreshToken(ticket);
        if (Strings.isNullOrEmpty(refreshToken)) {
            //创建刷新令牌
            refreshToken = DigestUtils.sha1Hex(sequence.nextId() + "|" + System.currentTimeMillis());
        }
        final Token token = Token.of(accessToken, refreshToken);
        //写入redis
        enhancedService.redisHandler(() -> {
            final ValueOperations<String, String> redisVals = redisTemplate.opsForValue();
            final Duration expire = authProperties.getRefreshTokenExpire();
            lockService.syncLockSingle(ticket.getId(), () -> {
                //存储刷新令牌
                final String refreshTokenKey = getRefreshTokenKey(ticket);
                redisVals.set(refreshTokenKey, token.getRefershToken(), expire);
                //存储票据数据
                final String ticketWithRefreshTokenKey = getTicketWithRefreshKey(token.getRefershToken());
                redisVals.set(ticketWithRefreshTokenKey, JsonUtils.toJson(objMapper, ticket), expire);
                //存储token-refresh关系
                final String tokenWithRefreshKey = getTokenWithRefreshKey(token.getRefershToken());
                redisVals.set(tokenWithRefreshKey, token.getAccessToken(), expire);
                //存储refresh-token关系
                final String refreshWithTokenKey = getRefreshWithTokenKey(token.getAccessToken());
                redisVals.set(refreshWithTokenKey, token.getRefershToken(), authProperties.getTokenExpire());
                //令牌限制处理
                limitService.limit(ticket, authProperties.getMaxLoginTotals(), token.getAccessToken());
            });
        });
        return token;
    }

    @Override
    public String refreshToken(@Nonnull final String refreshToken) {
        final Ticket ticket = parseRefreshToken(refreshToken);
        if (Objects.isNull(ticket)) {
            throw new TokenExpireException("刷新令牌已过期");
        }
        final AtomicReference<String> ref = new AtomicReference<>(null);
        lockService.syncLock(refreshToken, () -> {
            //创建访问令牌
            final String accessToken = JwtUtils.create(sequence.nextId() + "", ticket, authProperties.getTokenExpire());
            final Duration expire = authProperties.getRefreshTokenExpire();
            //写入redis
            enhancedService.redisHandler(() -> {
                final ValueOperations<String, String> redisVals = redisTemplate.opsForValue();
                //存储刷新令牌
                final String refreshTokenKey = getRefreshTokenKey(ticket);
                redisVals.set(refreshTokenKey, refreshToken, expire);
                //存储票据数据
                final String ticketWithRefreshTokenKey = getTicketWithRefreshKey(refreshToken);
                redisVals.set(ticketWithRefreshTokenKey, JsonUtils.toJson(objMapper, ticket), expire);
                //存储token-refresh关系
                final String tokenWithRefreshKey = getTokenWithRefreshKey(refreshToken);
                redisVals.set(tokenWithRefreshKey, accessToken, expire);
                //存储refresh-token关系
                final String refreshWithTokenKey = getRefreshWithTokenKey(accessToken);
                redisVals.set(refreshWithTokenKey, refreshToken, authProperties.getTokenExpire());
                //新令牌入队处理
                limitService.limitIn(ticket, accessToken);
                //新的访问令牌
                ref.set(accessToken);
            });
        });
        return ref.get();
    }

    @Override
    public Ticket parseToken(@Nonnull final String accessToken) throws TokenException {
        return JwtUtils.parse(accessToken, Ticket.class);
    }

    @Override
    public Ticket parseRefreshToken(@Nonnull final String refreshToken) {
        return enhancedService.redisHandler(() -> {
            //存储票据数据
            final String ticketWithRefreshTokenKey = getTicketWithRefreshKey(refreshToken);
            final String json = redisTemplate.opsForValue().get(ticketWithRefreshTokenKey);
            if (!Strings.isNullOrEmpty(json)) {
                return JsonUtils.fromJson(objMapper, json, Ticket.class);
            }
            return null;
        });
    }

    @Override
    public Ticket validToken(@Nonnull final String accessToken) {
        try {
            return parseToken(accessToken);
        } catch (Throwable e) {
            log.warn("validToken(accessToken: {})-exp: {}", accessToken, e.getMessage());
        }
        return null;
    }

    @Override
    public void delToken(@Nonnull final String accessToken) {
        if (Strings.isNullOrEmpty(accessToken)) {
            return;
        }
        enhancedService.redisHandler(() -> {
            //存储refresh-token关系
            final String refreshWithTokenKey = getRefreshWithTokenKey(accessToken);
            redisTemplate.delete(refreshWithTokenKey);
        });
    }

    @Override
    public String getRefreshToken(@Nonnull final String token) {
        return enhancedService.redisHandler(() -> {
            final String key = getRefreshWithTokenKey(token);
            return redisTemplate.opsForValue().get(key);
        });
    }

    private String getRefreshToken(@Nonnull final Ticket ticket) {
        return enhancedService.redisHandler(() -> {
            final String key = getRefreshTokenKey(ticket);
            return redisTemplate.opsForValue().get(key);
        });
    }

    @Override
    public String getToken(@Nonnull final String refreshToken) {
        return enhancedService.redisHandler(() -> {
            final String key = getTokenWithRefreshKey(refreshToken);
            return redisTemplate.opsForValue().get(key);
        });
    }
}
