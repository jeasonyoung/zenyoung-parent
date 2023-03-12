package top.zenyoung.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.apache.commons.codec.binary.Base64;
import top.zenyoung.common.model.UserPrincipal;

import javax.annotation.Nonnull;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 用认证工具
 *
 * @author young
 */
public class PrincipalUtils {
    private static final Charset CHARET = StandardCharsets.UTF_8;

    /**
     * 编码-将用户认证信息Base64
     *
     * @param objectMapper  ObjectMapper
     * @param userPrincipal 用户认证信息
     * @return Base64
     */
    public static String encode(@Nonnull final ObjectMapper objectMapper, @Nonnull final UserPrincipal userPrincipal) {
        final String json = JsonUtils.toJson(objectMapper, userPrincipal);
        return Base64.encodeBase64String(json.getBytes(CHARET));
    }

    /**
     * 解码-将base64转换为用户认证信息
     *
     * @param objectMapper ObjectMapper
     * @param encode       用户认证信息base64
     * @return 用户认证信息
     */
    public static UserPrincipal decode(@Nonnull final ObjectMapper objectMapper, @Nonnull final String encode) {
        if (!Strings.isNullOrEmpty(encode)) {
            final String json = new String(Base64.decodeBase64(encode), CHARET);
            if (!Strings.isNullOrEmpty(json)) {
                return JsonUtils.fromJson(objectMapper, json, UserPrincipal.class);
            }
        }
        return null;
    }
}
