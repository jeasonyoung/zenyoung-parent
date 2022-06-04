package top.zenyoung.security.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import top.zenyoung.common.util.JsonUtils;
import top.zenyoung.security.exception.TokenException;
import top.zenyoung.security.exception.TokenExpireException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;

/**
 * jwt工具类
 *
 * @author young
 */
@Slf4j
public class JwtUtils {
    private static final String JWT_ISS = "zenyoung";
    private static final String JWT_SECRECT = "zh-CN#hncs#young@zenyoung.top$(615616)&mxmf![&12345678901&]!#^*^";
    private static final String PAYLOAD = "sub";
    private static final SignatureAlgorithm ALG = SignatureAlgorithm.HS512;
    private static final ObjectMapper OBJ_MAPPER = new ObjectMapper();

    private static final SecretKey KEY;

    static {
        final byte[] encodedKey = Base64.decodeBase64(JWT_SECRECT);
        KEY = new SecretKeySpec(encodedKey, ALG.getJcaName());
    }

    /**
     * 创建Jwt串
     *
     * @param id     ID
     * @param data   数据
     * @param maxAge 生存期
     * @param <T>    数据类型
     * @return Jwt串
     */
    public static <T> String create(@Nonnull final String id, @Nonnull final T data, @Nullable final Duration maxAge) {
        final String json = JsonUtils.toJson(OBJ_MAPPER, data);
        final long nowMillis = System.currentTimeMillis();
        final Date now = new Date(nowMillis);
        final JwtBuilder builder = Jwts.builder()
                .setId(id)
                .setIssuedAt(now)
                .setSubject(json)
                .setIssuer(JWT_ISS)
                .signWith(ALG, KEY);
        //设置过期时间
        final long maxAgeMillis;
        if (Objects.nonNull(maxAge) && (maxAgeMillis = maxAge.toMillis()) > 0) {
            final long expMillis = nowMillis + maxAgeMillis;
            final Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        return builder.compact();
    }

    /**
     * 解析jwt串
     *
     * @param token     jwt串
     * @param dataClass 数据类型
     * @param <T>       数据类型
     * @return 数据对象
     * @throws TokenException 异常
     */
    public static <T> T parse(@Nonnull final String token, @Nonnull final Class<T> dataClass) throws TokenException {
        try {
            //解析 claims对象
            final Claims claims = Jwts.parser()
                    .setSigningKey(KEY)
                    .parseClaimsJws(token)
                    .getBody();
            if (Objects.nonNull(claims) && claims.containsKey(PAYLOAD)) {
                //解析对象
                final String json = (String) claims.get(PAYLOAD);
                return JsonUtils.fromJson(OBJ_MAPPER, json, dataClass);
            }
            throw new TokenException("token无效");
        } catch (ExpiredJwtException e) {
            log.error("parse(token: {},dataClass: {})-exp: {}", token, dataClass, e.getMessage());
            throw new TokenExpireException("token已过期");
        } catch (Exception e) {
            log.error("parse(token: {},dataClass: {})-exp: {}", token, dataClass, e.getMessage());
            throw new TokenException("token无效");
        }
    }
}
