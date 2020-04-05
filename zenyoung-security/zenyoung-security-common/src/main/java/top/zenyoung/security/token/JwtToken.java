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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtToken implements Token {
    private static final Duration DEF_EXPIRE = Duration.ofMillis(30 * 60 * 1000);
    private static final String DEF_TOKEN_SIGN_SLAT = JwtToken.class.getName();

    private static final Cache<String, SecretKey> SECRET_KEY_CACHE = CacheUtils.createCache();

    /**
     * 令牌有效期
     */
    private Duration expire;
    /**
     * 令牌签名盐值
     */
    private String tokenSignSlat;

    private static SecretKey generalKey(@Nonnull final String key) {
        return CacheUtils.getCacheValue(SECRET_KEY_CACHE, key, () -> {
            final byte[] encodeKey = Base64.encodeBase64((key + DEF_TOKEN_SIGN_SLAT).getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(encodeKey, 0, encodeKey.length, "AES");
        });
    }

    @Override
    public String createToken(@Nonnull final TokenTicket ticket) {
        try {
            //获取密钥
            final String slat = getTokenSignSlat();
            final SecretKey secretKey = generalKey(Strings.isNullOrEmpty(slat) ? DEF_TOKEN_SIGN_SLAT : slat);
            //计算过期时间
            final int timeout = (int) (getExpire() == null ? DEF_EXPIRE : getExpire()).getSeconds();
            final Date start = new Date();
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            calendar.add(Calendar.SECOND, timeout);
            final Date expire = calendar.getTime();
            //生成jwt令牌
            return Jwts.builder()
                    .setId(UUID.randomUUID().toString())
                    .setAudience("chaosw")
                    .setSubject("vod-codec-token")
                    .addClaims(ticket.toClaims())
                    .setIssuer("mgr-api")
                    .setIssuedAt(start)
                    .setNotBefore(start)
                    .setExpiration(expire)
                    .signWith(SignatureAlgorithm.HS512, secretKey)
                    .compact();
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public TokenTicket parseToken(@Nonnull final String json) throws TokenException {
        if (Strings.isNullOrEmpty(json)) {
            throw new TokenException("令牌为空!");
        }
        try {
            //获取密钥
            final String slat = getTokenSignSlat();
            final SecretKey secretKey = generalKey(Strings.isNullOrEmpty(slat) ? DEF_TOKEN_SIGN_SLAT : slat);
            //解码令牌
            final Claims claims = Jwts.parser().setSigningKey(secretKey)
                    .parseClaimsJws(json)
                    .getBody();
            return TokenTicket.create(claims);
        } catch (Throwable ex) {
            throw new TokenException(ex);
        }
    }
}
