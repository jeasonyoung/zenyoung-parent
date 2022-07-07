package top.zenyoung.security.token.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.common.sequence.Sequence;
import top.zenyoung.common.util.BeanCacheUtils;
import top.zenyoung.common.util.JsonUtils;
import top.zenyoung.redis.lock.LockService;
import top.zenyoung.security.config.SecurityProperties;
import top.zenyoung.security.exception.TokenException;
import top.zenyoung.security.exception.TokenExpireException;
import top.zenyoung.security.auth.Ticket;
import top.zenyoung.security.auth.Token;
import top.zenyoung.security.token.TokenLimitService;
import top.zenyoung.security.token.TokenService;
import top.zenyoung.security.util.Constants;
import top.zenyoung.security.util.JwtUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 令牌服务接口实现
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final ApplicationContext context;

    private <T> String toJson(@Nonnull final T data) {
        return BeanCacheUtils.function(context, ObjectMapper.class, om -> JsonUtils.toJson(om, data));
    }

    private Ticket fromJson(@Nonnull final String json) {
        return BeanCacheUtils.function(context, ObjectMapper.class, om -> JsonUtils.fromJson(om, json, Ticket.class));
    }

    private <T> String createJwt(@Nonnull final Long id, @Nonnull final T data, @Nullable final Duration maxAge) {
        return BeanCacheUtils.function(context, ObjectMapper.class, om -> JwtUtils.create(om, id + "", data, maxAge));
    }

    private <T> T getProperties(@Nonnull final Function<SecurityProperties, T> handler) {
        return BeanCacheUtils.function(context, SecurityProperties.class, handler);
    }

    private Long getSeqNextId() {
        return BeanCacheUtils.function(context, IdSequence.class, Sequence::nextId);
    }

    private void redisHandler(@Nonnull final Consumer<StringRedisTemplate> consumer) {
        BeanCacheUtils.consumer(context, StringRedisTemplate.class, consumer);
    }

    private <T> T redisHandler(@Nonnull final Function<StringRedisTemplate, T> handler) {
        return BeanCacheUtils.function(context, StringRedisTemplate.class, handler);
    }

    private void syncLock(@Nonnull final String key, @Nonnull final Duration expire,
                          @Nonnull final Runnable lockHandler, @Nullable final Runnable unlockHandler) {
        BeanCacheUtils.consumer(context, LockService.class, srv -> srv.sync(key, expire, lockHandler, unlockHandler));
    }

    private static String getDelayAccessKeyWithRefresh(@Nonnull final String refreshToken) {
        return Constants.AUTH_REFRESH_TOKEN_PREFIX + Constants.join("delay-access-token", refreshToken);
    }

    private void writeCacheHandler(@Nonnull final Ticket ticket, @Nonnull final String accessToken, @Nonnull final String refreshToken) {
        if (!Strings.isNullOrEmpty(accessToken) && !Strings.isNullOrEmpty(refreshToken)) {
            try {
                //缓存刷新令牌键
                final String refreshTokenKey = Constants.getAuthRefreshWithTicketKey(ticket);
                //缓存票据键
                final String ticketWithRefreshKey = Constants.getAuthTicketWithRefreshKey(refreshToken);
                //缓存token-refresh关系键
                final String tokenWithRefreshKey = Constants.getAuthAccessWithRefreshKey(refreshToken);
                //存储refresh-token关系
                final String refreshWithTokenKey = Constants.getAuthRefreshWithAccessKey(accessToken);
                //访问令牌有效期/刷新令牌有效期
                final Duration accessTokenExpire = getProperties(SecurityProperties::getAccessTokenExpire);
                final Duration refreshTokenExpire = getProperties(SecurityProperties::getRefreshTokenExpire);
                if (Objects.nonNull(accessTokenExpire) && Objects.nonNull(refreshTokenExpire)) {
                    //获取redis
                    redisHandler(template -> {
                        //获取刷新令牌过期剩余ttl
                        final Long ttl = template.getExpire(refreshTokenKey, TimeUnit.SECONDS);
                        final Duration refreshExpire = Objects.isNull(ttl) ? refreshTokenExpire : refreshTokenExpire.plusSeconds(Math.max(ttl, 0));
                        //
                        final ValueOperations<String, String> redisVals = template.opsForValue();
                        //缓存刷新令牌
                        redisVals.set(refreshTokenKey, refreshToken, refreshExpire);
                        //缓存票据信息
                        redisVals.set(ticketWithRefreshKey, toJson(ticket), refreshExpire);
                        //缓存token-refresh关系
                        redisVals.set(tokenWithRefreshKey, accessToken, refreshExpire);
                        //存储refresh-token关系
                        redisVals.set(refreshWithTokenKey, refreshToken, accessTokenExpire);
                    });
                }
            } catch (Throwable e) {
                log.warn("writeCacheHandler(ticket: {},accessToken: {},refreshToken: {})-exp: {}", ticket, accessToken, refreshToken, e.getMessage());
            }
        }
    }

    private void limit(@Nonnull final Ticket ticket, @Nonnull final Integer maxTokenCount, @Nonnull final String accessToken) {
        BeanCacheUtils.consumer(context, TokenLimitService.class, bean -> bean.limit(ticket, maxTokenCount, accessToken));
    }

    private void limitIn(@Nonnull final Ticket ticket, @Nonnull final String accessToken) {
        BeanCacheUtils.consumer(context, TokenLimitService.class, bean -> bean.limitIn(ticket, accessToken));
    }

    @Override
    public Token createToken(@Nonnull final Ticket ticket) {
        //创建访问令牌
        final Long accessTokenId = getSeqNextId();
        final Duration accessTokenExpire = getProperties(SecurityProperties::getAccessTokenExpire);
        final String accessToken = createJwt(accessTokenId, ticket, accessTokenExpire);
        //检查刷新令牌
        String refreshToken = getRefreshToken(ticket);
        if (Strings.isNullOrEmpty(refreshToken)) {
            final Long refreshTokenId = getSeqNextId();
            //创建刷新令牌
            refreshToken = DigestUtils.sha1Hex(refreshTokenId + "|" + System.currentTimeMillis());
        }
        final Token token = Token.of(accessToken, refreshToken);
        //写入缓存
        writeCacheHandler(ticket, token.getAccessToken(), token.getRefershToken());
        //令牌限制处理
        final Integer max = getProperties(SecurityProperties::getMaxLoginTotals);
        if (Objects.nonNull(max) && max > 0) {
            limit(ticket, max, token.getAccessToken());
        }
        return token;
    }

    private String getDelayAccessToken(@Nonnull final String refreshToken) {
        if (Strings.isNullOrEmpty(refreshToken)) {
            return null;
        }
        return redisHandler(template -> {
            final String delayAccessTokenKey = getDelayAccessKeyWithRefresh(refreshToken);
            return template.opsForValue().get(delayAccessTokenKey);
        });
    }

    private void setDelayAccessToken(@Nonnull final String refreshToken, @Nonnull final String accessToken) {
        if (!Strings.isNullOrEmpty(refreshToken) && !Strings.isNullOrEmpty(accessToken)) {
            redisHandler(template -> {
                final String delayAccessTokenKey = getDelayAccessKeyWithRefresh(refreshToken);
                template.opsForValue().set(delayAccessTokenKey, accessToken, Duration.ofSeconds(30));
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
        syncLock(refreshToken, Duration.ofMillis(200), () -> {
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
            final Long accessTokenId = getSeqNextId();
            final Duration accessTokenExpire = getProperties(SecurityProperties::getAccessTokenExpire);
            accessToken = createJwt(accessTokenId, ticket, accessTokenExpire);
            //写入缓存
            writeCacheHandler(ticket, accessToken, refreshToken);
            //延时令牌处理
            setDelayAccessToken(refreshToken, accessToken);
            //新令牌入队处理
            limitIn(ticket, accessToken);
            //新的访问令牌
            ref.set(accessToken);
        }, () -> {
            //获取旧令牌
            //缓存token-refresh关系键
            final String tokenWithRefreshKey = Constants.getAuthAccessWithRefreshKey(refreshToken);
            redisHandler(template -> {
                final String oldAccessToken = template.opsForValue().get(tokenWithRefreshKey);
                if (!Strings.isNullOrEmpty(oldAccessToken)) {
                    ref.set(oldAccessToken);
                }
            });
        });
        return ref.get();
    }

    @Override
    public Ticket parseToken(@Nonnull final String accessToken) throws TokenException {
        return BeanCacheUtils.function(context, ObjectMapper.class, om -> JwtUtils.parse(om, accessToken, Ticket.class));
    }

    @Override
    public Ticket parseRefreshToken(@Nonnull final String refreshToken) {
        return redisHandler(template -> {
            //存储票据数据
            final String ticketWithRefreshTokenKey = Constants.getAuthTicketWithRefreshKey(refreshToken);
            final String json = template.opsForValue().get(ticketWithRefreshTokenKey);
            if (!Strings.isNullOrEmpty(json)) {
                return fromJson(json);
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
        redisHandler(template -> {
            //存储refresh-token关系
            final String refreshWithTokenKey = Constants.getAuthRefreshWithAccessKey(accessToken);
            template.delete(refreshWithTokenKey);
        });
    }

    @Override
    public String getRefreshToken(@Nonnull final String accessToken) {
        return redisHandler(template -> {
            final String key = Constants.getAuthRefreshWithAccessKey(accessToken);
            return template.opsForValue().get(key);
        });
    }

    private String getRefreshToken(@Nonnull final Ticket ticket) {
        return redisHandler(template -> {
            final String key = Constants.getAuthRefreshWithTicketKey(ticket);
            return template.opsForValue().get(key);
        });
    }

    @Override
    public String getToken(@Nonnull final String refreshToken) {
        return redisHandler(template -> {
            final String key = Constants.getAuthAccessWithRefreshKey(refreshToken);
            return template.opsForValue().get(key);
        });
    }
}
