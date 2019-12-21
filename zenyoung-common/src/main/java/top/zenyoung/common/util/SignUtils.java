package top.zenyoung.common.util;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 参数签名-工具类
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/11/23 7:00 下午
 **/
@Slf4j
public class SignUtils {
    private static final String KK_JOIN = ".", KV_JOIN = "=", KV_KV_JOIN = "&";

    public static String buildSign(@Nonnull final Map<String, Serializable> params, @Nullable final String secret) {
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
        if (val instanceof String) {
            if (Strings.isNullOrEmpty((String) val)) {
                return null;
            }
            return Lists.newArrayList((Strings.isNullOrEmpty(parentKey) ? key : parentKey + KK_JOIN + key) + KV_JOIN + val);
        }
        final String strVal = val.toString();
        if (!Strings.isNullOrEmpty(strVal)) {
            return Lists.newArrayList((Strings.isNullOrEmpty(parentKey) ? key : parentKey + KK_JOIN + key) + KV_JOIN + val);
        }
        return null;
    }
}