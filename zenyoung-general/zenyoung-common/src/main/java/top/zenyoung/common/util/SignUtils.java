package top.zenyoung.common.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 签名工具类
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/2/13 1:21 下午
 **/
@Slf4j
public class SignUtils {
    private static final String KK_JOIN = ".", KV_JOIN = "=", KV_KV_JOIN = "&", COLLECTION_JOIN = "";

    @SuppressWarnings({"unchecked"})
    private static List<String> buildParamKeyVal(@Nullable final String parentKey, @Nonnull final Map.Entry<String, Serializable> entry) {
        final String key = entry.getKey();
        final Serializable val = entry.getValue();
        if (Strings.isNullOrEmpty(key) || val == null) {
            return null;
        }
        if (val instanceof Map) {
            return ((Map<String, Serializable>) val).entrySet().stream()
                    .filter(child -> child != null && !Strings.isNullOrEmpty(child.getKey()) && child.getValue() != null)
                    .map(child -> buildParamKeyVal(Strings.isNullOrEmpty(parentKey) ? key : parentKey + KK_JOIN + key, child))
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        if (val instanceof Collection) {
            if (CollectionUtils.isEmpty((Collection<?>) val)) {
                return null;
            }
            final String strVal = Joiner.on(COLLECTION_JOIN).skipNulls().join(((Collection<?>) val).stream()
                    .map(v -> {
                        String ret = null;
                        if (v != null) {
                            if (v instanceof String) {
                                ret = (String) v;
                            } else {
                                ret = v.toString();
                            }
                        }
                        return Strings.isNullOrEmpty(ret) ? null : ret;
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(str -> str))
                    .collect(Collectors.toList())
            );
            if (Strings.isNullOrEmpty(strVal)) {
                return null;
            }
            return Lists.newArrayList((Strings.isNullOrEmpty(parentKey) ? key : parentKey + KK_JOIN + key) + KV_JOIN + strVal);
        }
        if (val instanceof Number) {
            if (((Number) val).doubleValue() <= 0) {
                return null;
            }
            return Lists.newArrayList((Strings.isNullOrEmpty(parentKey) ? key : parentKey + KK_JOIN + key) + KV_JOIN + val);
        }
        if (val instanceof Boolean) {
            if (!((Boolean) val)) {
                return null;
            }
            return Lists.newArrayList((Strings.isNullOrEmpty(parentKey) ? key : parentKey + KK_JOIN + key) + KV_JOIN + val);
        }
        if (val instanceof String) {
            if (Strings.isNullOrEmpty((String) val)) {
                return null;
            }
            return Lists.newArrayList((Strings.isNullOrEmpty(parentKey) ? key : parentKey + KK_JOIN + key) + KV_JOIN + val);
        }
        final String strVal = val.toString();
        if (!Strings.isNullOrEmpty(strVal)) {
            return Lists.newArrayList((Strings.isNullOrEmpty(parentKey) ? key : parentKey + KK_JOIN + key) + KV_JOIN + strVal);
        }
        return null;
    }

    /**
     * 创建参数签名
     *
     * @param params 签名参数集合
     * @param secret 签名密钥
     * @return 参数签名
     */
    public static String createSign(@Nonnull final Map<String, Serializable> params, @Nullable final String secret) {
        Assert.notEmpty(params, "'params'签名参数集合不能为空!");
        final String source = params.entrySet().stream()
                .filter(entry -> !Strings.isNullOrEmpty(entry.getKey()) && entry.getValue() != null)
                .map(entry -> buildParamKeyVal(null, entry))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(kv -> Splitter.on(KV_JOIN).splitToList(kv).get(0)))
                .collect(Collectors.joining(KV_KV_JOIN));
        //签名前字符串传
        final String sourceVal = Strings.isNullOrEmpty(secret) ? source : source + KV_KV_JOIN + secret;
        //参数签名处理
        final String sign = DigestUtils.sha1Hex(sourceVal.getBytes(StandardCharsets.UTF_8));
        log.info("buildSign[{}]=> {}", sign, sourceVal);
        return sign;
    }
}
