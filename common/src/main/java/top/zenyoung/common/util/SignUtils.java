package top.zenyoung.common.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

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
 **/
@Slf4j
@UtilityClass
public class SignUtils {
    private static final String KK_JOIN = ".", KV_JOIN = "=", KV_KV_JOIN = "&", COLLECTION_JOIN = ",";

    @SuppressWarnings({"unchecked"})
    private static List<String> buildParamKeyVal(@Nullable final String parentKey, @Nonnull final Map.Entry<String, Serializable> entry) {
        //键名
        final String key = entry.getKey();
        //键值
        final Serializable val = entry.getValue();
        //检查键名或键值是否为空
        if (Strings.isNullOrEmpty(key) || val == null) {
            return Lists.newArrayList();
        }
        //检查键值是否为Map
        if (val instanceof Map) {
            return ((Map<String, Serializable>) val).entrySet().stream()
                    .filter(child -> child != null && !Strings.isNullOrEmpty(child.getKey()) && child.getValue() != null)
                    .map(child -> buildParamKeyVal(Strings.isNullOrEmpty(parentKey) ? key : parentKey + KK_JOIN + key, child))
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        //检查键值是否为集合
        if (val instanceof Collection) {
            //集合处理
            return buildParamCollection(parentKey, key, (Collection<?>) val);
        }
        //检查键值为数值
        if (val instanceof Number) {
            //排除数值小于等于0
            if (((Number) val).doubleValue() <= 0) {
                return Lists.newArrayList();
            }
            //数值处理
            return Lists.newArrayList((Strings.isNullOrEmpty(parentKey) ? key : parentKey + KK_JOIN + key) + KV_JOIN + val);
        }
        //检查键值为布尔值
        if (val instanceof Boolean) {
            //排除布尔值为false
            if (!((boolean) val)) {
                return Lists.newArrayList();
            }
            //布尔值处理
            return Lists.newArrayList((Strings.isNullOrEmpty(parentKey) ? key : parentKey + KK_JOIN + key) + KV_JOIN + val);
        }
        //检查键值为字符串
        if (val instanceof String) {
            //排除空字符串
            if (Strings.isNullOrEmpty((String) val)) {
                return Lists.newArrayList();
            }
            //字符串处理
            return Lists.newArrayList((Strings.isNullOrEmpty(parentKey) ? key : parentKey + KK_JOIN + key) + KV_JOIN + val);
        }
        //普通对象处理
        final String strVal = val.toString();
        if (!Strings.isNullOrEmpty(strVal)) {
            return Lists.newArrayList((Strings.isNullOrEmpty(parentKey) ? key : parentKey + KK_JOIN + key) + KV_JOIN + strVal);
        }
        return Lists.newArrayList();
    }

    @SuppressWarnings({"unchecked"})
    private static List<String> buildParamCollection(@Nullable final String parentKey, @Nonnull final String key, @Nonnull final Collection<?> vals) {
        if (!vals.isEmpty()) {
            //集合内容处理
            final String strVal = Joiner.on(COLLECTION_JOIN).skipNulls().join(((Collection<?>) vals).stream()
                    .map(v -> {
                        if (v != null) {
                            //集合成员为Map
                            if (v instanceof Map) {
                                return Joiner.on(COLLECTION_JOIN).skipNulls().join(
                                        ((Map<String, Serializable>) v).entrySet().stream()
                                                .map(e -> buildParamKeyVal(Strings.isNullOrEmpty(parentKey) ? key : parentKey + KK_JOIN + key, e))
                                                .filter(Objects::nonNull)
                                                .flatMap(Collection::stream)
                                                .sorted(Comparator.comparing(kv -> Splitter.on(KV_JOIN).splitToList(kv).get(0)))
                                                .collect(Collectors.toList())
                                );
                            }
                            //集合成员为字符串
                            if (v instanceof String) {
                                return (String) v;
                            } else {
                                return v.toString();
                            }
                        }
                        return null;
                    })
                    .filter(sv -> !Strings.isNullOrEmpty(sv))
                    .sorted(Comparator.comparing(str -> str))
                    .collect(Collectors.toList())
            );
            //检查集合内容是否为空
            if (!Strings.isNullOrEmpty(strVal)) {
                //输出集合内容
                return Lists.newArrayList((Strings.isNullOrEmpty(parentKey) ? key : parentKey + KK_JOIN + key) + KV_JOIN + strVal);
            }
        }
        return Lists.newArrayList();
    }

    /**
     * 创建参数签名
     *
     * @param params 签名参数集合
     * @param secret 签名密钥
     * @return 参数签名
     */
    public static String createSign(@Nonnull final Map<String, Serializable> params, @Nullable final String secret) {
        if (params.isEmpty()) {
            throw new IllegalArgumentException("'params'签名参数集合不能为空!");
        }
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
