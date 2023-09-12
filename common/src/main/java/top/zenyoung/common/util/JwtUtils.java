package top.zenyoung.common.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.common.exception.ServiceException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.Objects;

/**
 * Jwt工具类
 *
 * @author young
 */
@Slf4j
@UtilityClass
public class JwtUtils {
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> String generate(@Nonnull final JWSAlgorithm algorithm, @Nullable final JWSSigner signer,
                                      @Nonnull final T data) {
        try {
            final String body = JsonUtils.toJson(MAPPER, data);
            final JWSHeader header = new JWSHeader.Builder(algorithm)
                    .type(JOSEObjectType.JWT)
                    .build();
            final Payload payload = new Payload(body);
            final JWSObject jws = new JWSObject(header, payload);
            if (Objects.nonNull(signer)) {
                jws.sign(signer);
            }
            return jws.serialize();
        } catch (JOSEException e) {
            log.error("create(algorithm: {},signer: {},data: {})-exp: {}", algorithm, signer, data, e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    public static <T> String generateHmac(@Nonnull final T data, @Nonnull final String secret) throws KeyLengthException {
        final MACSigner signer = new MACSigner(secret);
        return generate(JWSAlgorithm.HS256, signer, data);
    }

    public static <T> T parse(@Nullable final JWSVerifier verifier, @Nonnull final Class<T> cls, @Nonnull final String jwt) {
        try {
            final JWSObject obj = JWSObject.parse(jwt);
            if (Objects.nonNull(verifier) && !obj.verify(verifier)) {
                //验证失败
                return null;
            }
            final String json = obj.getPayload().toString();
            return JsonUtils.fromJson(MAPPER, json, cls);
        } catch (ParseException | JOSEException e) {
            log.error("parse(verifier: {},cls: {},jwt: {})-exp: {}", verifier, cls, jwt, e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    public static <T> T parseHmac(@Nonnull final Class<T> cls, @Nonnull final String jwt, @Nonnull final String secret) throws JOSEException {
        final MACVerifier verifier = new MACVerifier(secret);
        return parse(verifier, cls, jwt);
    }
}
