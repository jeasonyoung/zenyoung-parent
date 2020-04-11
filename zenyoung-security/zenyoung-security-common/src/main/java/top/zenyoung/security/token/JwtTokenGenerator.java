package top.zenyoung.security.token;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import top.zenyoung.common.util.CacheUtils;
import top.zenyoung.security.exception.TokenException;

import javax.annotation.Nonnull;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Jwt令牌
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/3/19 4:44 下午
 **/
@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenGenerator implements TokenGenerator {
    private static final Duration DEF_EXPIRE = Duration.ofMinutes(30);
    private static final String DEF_SIGN_SLAT = JwtTokenGenerator.class.getName();

    private static final Cache<String, SecretKey> SECRET_KEY_CACHE = CacheUtils.createCache();

    /**
     * 令牌有效期
     */
    private Duration expire;
    /**
     * 令牌签名盐值
     */
    private String signSlat;

    private static SecretKey generalKey(@Nonnull final String key) {
        log.debug("generalKey(key: {})...", key);
        return CacheUtils.getCacheValue(SECRET_KEY_CACHE, key, () -> {
            final byte[] encodeKey = Base64.encodeBase64((key + DEF_SIGN_SLAT).getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(encodeKey, 0, encodeKey.length, "AES");
        });
    }

    @Override
    public String createToken(@Nonnull final Ticket ticket) {
        try {
            //获取密钥
            final String slat = getSignSlat();
            final SecretKey secretKey = generalKey(Strings.isNullOrEmpty(slat) ? DEF_SIGN_SLAT : slat);
            //计算过期时间
            final int timeout = (int) (getExpire() == null ? DEF_EXPIRE : getExpire()).getSeconds();
            final Date start = new Date();
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            calendar.add(Calendar.SECOND, timeout);
            //生成jwt令牌
            return Jwts.builder()
                    .setId(UUID.randomUUID().toString())
                    .addClaims(ticket.toClaims())
                    .setIssuedAt(start)
                    .setNotBefore(start)
                    .setExpiration(calendar.getTime())
                    .signWith(SignatureAlgorithm.HS512, secretKey)
                    .compact();
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Ticket parseToken(@Nonnull final String token) throws TokenException {
        log.debug("parseToken(token: {})...", token);
        if (Strings.isNullOrEmpty(token)) {
            throw new TokenException("令牌为空!");
        }
        try {
            //获取密钥
            final String slat = getSignSlat();
            final SecretKey secretKey = generalKey(Strings.isNullOrEmpty(slat) ? DEF_SIGN_SLAT : slat);
            //解码令牌
            final Claims claims = Jwts.parser().setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return Ticket.create(claims);
        } catch (Throwable ex) {
            throw new TokenException(ex);
        }
    }
}
