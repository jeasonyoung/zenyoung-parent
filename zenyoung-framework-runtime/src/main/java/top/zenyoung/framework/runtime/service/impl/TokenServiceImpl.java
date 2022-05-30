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
import top.zenyoung.framework.Constants;
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
import java.util.concurrent.TimeUnit;
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
    private final StringRedisTemplate redisTemplate;
    private final IdSequence sequence;
    private final ObjectMapper objMapper;
    private final AuthProperties authProperties;
    private final RedisEnhancedService enhancedService;
    private final SyncLockService lockService;
    private final TokenLimitService limitService;

    private static String getDelayAccessKeyWithRefresh(@Nonnull final String refreshToken) {
        return Constants.AUTH_REFRESH_TOKEN_PREFIX + Constants.join("delay-access-token", refreshToken);
    }

    private void writeCacheHandler(@Nonnull final Ticket ticket, @Nonnull final String accessToken, @Nonnull final String refreshToken) {
        if (!Strings.isNullOrEmpty(accessToken) && !Strings.isNullOrEmpty(refreshToken)) {
            //缓存刷新令牌键
            final String refreshTokenKey = Constants.getAuthRefreshWithTicketKey(ticket);
            //缓存票据键
            final String ticketWithRefreshKey = Constants.getAuthTicketWithRefreshKey(refreshToken);
            //缓存token-refresh关系键
            final String tokenWithRefreshKey = Constants.getAuthAccessWithRefreshKey(refreshToken);
            //存储refresh-token关系
            final String refreshWithTokenKey = Constants.getAuthRefreshWithAccessKey(accessToken);
            //访问令牌有效期/刷新令牌有效期
            final Duration accessTokenExpire = authProperties.getAccessTokenExpire(), refreshTokenExpire = authProperties.getRefreshTokenExpire();
            enhancedService.redisHandler(() -> {
                //获取刷新令牌过期剩余ttl
                final Long expire = redisTemplate.getExpire(refreshTokenKey, TimeUnit.SECONDS);
                //获取redis
                final ValueOperations<String, String> redisVals = redisTemplate.opsForValue();
                //缓存刷新令牌
                redisVals.set(refreshTokenKey, refreshToken, Objects.isNull(expire) ? refreshTokenExpire : refreshTokenExpire.plusSeconds(Math.max(expire, 0)));
                //缓存票据信息
                redisVals.set(ticketWithRefreshKey, JsonUtils.toJson(objMapper, ticket), refreshTokenExpire);
                //缓存token-refresh关系
                redisVals.set(tokenWithRefreshKey, accessToken, refreshTokenExpire);
                //存储refresh-token关系
                redisVals.set(refreshWithTokenKey, refreshToken, accessTokenExpire);
            });
        }
    }

    @Override
    public Token createToken(@Nonnull final Ticket ticket) {
        final Long tokenId = sequence.nextId();
        //创建访问令牌
        final String accessToken = JwtUtils.create(tokenId + "", ticket, authProperties.getAccessTokenExpire());
        //检查刷新令牌
        String refreshToken = getRefreshToken(ticket);
        if (Strings.isNullOrEmpty(refreshToken)) {
            //创建刷新令牌
            refreshToken = DigestUtils.sha1Hex(sequence.nextId() + "|" + System.currentTimeMillis());
        }
        final Token token = Token.of(accessToken, refreshToken);
        //写入redis
        lockService.syncLockSingle(ticket.getId(), () -> {
            //写入缓存
            writeCacheHandler(ticket, token.getAccessToken(), token.getRefershToken());
            //令牌限制处理
            limitService.limit(ticket, authProperties.getMaxLoginTotals(), token.getAccessToken());
        });
        return token;
    }

    private String getDelayAccessToken(@Nonnull final String refreshToken) {
        if (Strings.isNullOrEmpty(refreshToken)) {
            return null;
        }
        return enhancedService.redisHandler(() -> {
            final String delayAccessTokenKey = getDelayAccessKeyWithRefresh(refreshToken);
            return redisTemplate.opsForValue().get(delayAccessTokenKey);
        });
    }

    private void setDelayAccessToken(@Nonnull final String refreshToken, @Nonnull final String accessToken) {
        if (!Strings.isNullOrEmpty(refreshToken) && !Strings.isNullOrEmpty(accessToken)) {
            enhancedService.redisHandler(() -> {
                final String delayAccessTokenKey = getDelayAccessKeyWithRefresh(refreshToken);
                redisTemplate.opsForValue().set(delayAccessTokenKey, accessToken, Duration.ofSeconds(30));
            });
        }
    }

    @Override
    public String refreshToken(@Nonnull final String refreshToken) {
        final Ticket ticket = parseRefreshToken(refreshToken);
        if (Objects.isNull(ticket)) {
            throw new TokenExpireException("刷新令牌已过期");
        }
        final AtomicReference<String> ref = new AtomicReference<>(null);
        lockService.syncLock(refreshToken, () -> {
            //获取最新访问令牌
            String accessToken = getToken(refreshToken);
            if (!Strings.isNullOrEmpty(accessToken)) {
                //获取30秒延时处理
                final String delayAccessToken = getDelayAccessToken(refreshToken);
                if (!Strings.isNullOrEmpty(delayAccessToken) && delayAccessToken.equals(accessToken)) {
                    ref.set(accessToken);
                    return;
                }
            }
            //创建访问令牌
            accessToken = JwtUtils.create(sequence.nextId() + "", ticket, authProperties.getAccessTokenExpire());
            //写入缓存
            writeCacheHandler(ticket, accessToken, refreshToken);
            //延时令牌处理
            setDelayAccessToken(refreshToken, accessToken);
            //新令牌入队处理
            limitService.limitIn(ticket, accessToken);
            //新的访问令牌
            ref.set(accessToken);
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
            final String ticketWithRefreshTokenKey = Constants.getAuthTicketWithRefreshKey(refreshToken);
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
            final String refreshWithTokenKey = Constants.getAuthRefreshWithAccessKey(accessToken);
            redisTemplate.delete(refreshWithTokenKey);
        });
    }

    @Override
    public String getRefreshToken(@Nonnull final String accessToken) {
        return enhancedService.redisHandler(() -> {
            final String key = Constants.getAuthRefreshWithAccessKey(accessToken);
            return redisTemplate.opsForValue().get(key);
        });
    }

    private String getRefreshToken(@Nonnull final Ticket ticket) {
        return enhancedService.redisHandler(() -> {
            final String key = Constants.getAuthRefreshWithTicketKey(ticket);
            return redisTemplate.opsForValue().get(key);
        });
    }

    @Override
    public String getToken(@Nonnull final String refreshToken) {
        return enhancedService.redisHandler(() -> {
            final String key = Constants.getAuthAccessWithRefreshKey(refreshToken);
            return redisTemplate.opsForValue().get(key);
        });
    }
}
