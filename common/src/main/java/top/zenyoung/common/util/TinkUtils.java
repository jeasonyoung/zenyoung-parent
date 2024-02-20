package top.zenyoung.common.util;

import com.google.crypto.tink.*;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.jwt.*;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import top.zenyoung.common.exception.ServiceException;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.security.GeneralSecurityException;

/**
 * Google Tink
 *
 * @author young
 */
@Slf4j
@UtilityClass
public class TinkUtils {
    private static final String TINK_KEYS = "tink-keys.json";
    private static final KeysetHandle KEYSET_HANDLE;
    private static final Aead AEAD;
    private static final JwtPublicKeySign JWT_SIGNER;
    private static final JwtPublicKeyVerify JWT_VERIFY;

    static {
        try {
            //对称加密
            AeadConfig.register();
            //jwt
            JwtSignatureConfig.register();
            //秘钥处理
            final ClassPathResource resource = new ClassPathResource(TINK_KEYS);
            if (resource.exists()) {
                //加载秘钥文件
                final InputStream is = resource.getInputStream();
                KEYSET_HANDLE = CleartextKeysetHandle.read(JsonKeysetReader.withInputStream(is));
            } else {
                //生成秘钥
                final var keyTemplate = AeadKeyTemplates.AES256_CTR_HMAC_SHA256;
                final var parameters = TinkProtoParametersFormat.parse(keyTemplate.toByteArray());
                KEYSET_HANDLE = KeysetHandle.generateNew(parameters);
            }
            //对称加/解密
            AEAD = KEYSET_HANDLE.getPrimitive(Aead.class);
            //jwt
            JWT_SIGNER = KEYSET_HANDLE.getPrimitive(JwtPublicKeySign.class);
            JWT_VERIFY = KEYSET_HANDLE.getPrimitive(JwtPublicKeyVerify.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ServiceException(e);
        }
    }

    /**
     * 加密
     *
     * @param raw 明文
     * @return 密文
     * @throws GeneralSecurityException 加密异常
     */
    public static byte[] encrypt(final byte[] raw) throws GeneralSecurityException {
        return AEAD.encrypt(raw, null);
    }

    /**
     * 解密
     *
     * @param cipher 密文
     * @return 明文
     * @throws GeneralSecurityException 解密异常
     */
    public static byte[] decrypt(final byte[] cipher) throws GeneralSecurityException {
        return AEAD.decrypt(cipher, null);
    }

    /**
     * 创建Jwt
     *
     * @param rawJwt 源Jwt
     * @return jwt串
     * @throws GeneralSecurityException 创建异常
     */
    public static String createJwt(@Nonnull final RawJwt rawJwt) throws GeneralSecurityException {
        return JWT_SIGNER.signAndEncode(rawJwt);
    }

    /**
     * 校验Jwt
     *
     * @param validator jwt验证器
     * @param jwt       Jwt串
     * @throws GeneralSecurityException 校验异常
     */
    public static VerifiedJwt verifyJwt(@Nonnull final JwtValidator validator, @Nonnull final String jwt) throws GeneralSecurityException {
        return JWT_VERIFY.verifyAndDecode(jwt, validator);
    }
}
