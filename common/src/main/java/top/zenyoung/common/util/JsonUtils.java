package top.zenyoung.common.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Json工具类
 *
 * @author yangyong
 * @version 1.0
 * date 2020/6/30 2:38 下午
 **/
@UtilityClass
public class JsonUtils {

    /**
     * 数据JSON化处理
     *
     * @param objectMapper JSON处理器
     * @param data         数据
     * @param <T>          数据类型
     * @return json字符数组
     */
    @SneakyThrows({})
    public static <T> byte[] toJsonBytes(@Nonnull final ObjectMapper objectMapper, @Nonnull final T data) {
        return objectMapper.writeValueAsBytes(data);
    }

    /**
     * 数据JSON化处理
     *
     * @param objectMapper JSON处理器
     * @param data         数据
     * @param <T>          数据类型
     * @return json字符串
     */
    @SneakyThrows({})
    public static <T> String toJson(@Nonnull final ObjectMapper objectMapper, @Nonnull final T data) {
        return objectMapper.writeValueAsString(data);
    }

    /**
     * 将JSON字节数组反序列化为对象
     *
     * @param objectMapper JSON处理器
     * @param jsonBytes    字节数组
     * @param cls          数据类型class
     * @param <R>          目标数据类型
     * @return 反序列为对象
     */
    @SneakyThrows({})
    public static <R> R fromJsonBytes(@Nonnull final ObjectMapper objectMapper, @Nonnull final byte[] jsonBytes,
                                      @Nonnull final Class<R> cls) {
        return objectMapper.readValue(jsonBytes, cls);
    }

    /**
     * 将JSON字符串反序列为对象
     *
     * @param objectMapper JSON处理器
     * @param json         json字符串
     * @param cls          数据类型class
     * @param <R>          目标数据类型
     * @return 反序列为对象
     */
    @SneakyThrows({})
    public static <R> R fromJson(@Nonnull final ObjectMapper objectMapper, @Nonnull final String json,
                                 @Nonnull final Class<R> cls) {
        if (!Strings.isNullOrEmpty(json)) {
            return objectMapper.readValue(json, cls);
        }
        return null;
    }

    /**
     * 将数据流转换为对象
     *
     * @param objectMapper JSON处理器
     * @param inputStream  输入流
     * @param cls          数据类型class
     * @param <R>          数据类型
     * @return 数据对象
     */
    @SneakyThrows({})
    public static <R> R fromStream(@Nonnull final ObjectMapper objectMapper, @Nonnull final InputStream inputStream,
                                   @Nonnull final Class<R> cls) {
        return objectMapper.readValue(inputStream, cls);
    }

    /**
     * 将Map转换为对象
     *
     * @param objectMapper JSON处理器
     * @param map          Map数据
     * @param cls          目标数据类型Class
     * @param <R>          目标数据类型
     * @return 对象数据
     */
    public static <R> R fromMap(@Nonnull final ObjectMapper objectMapper, @Nonnull final Map<String, Serializable> map,
                                @Nonnull final Class<R> cls) {
        final String json = toJson(objectMapper, map);
        if (!Strings.isNullOrEmpty(json)) {
            return fromJson(objectMapper, json, cls);
        }
        return null;
    }

    /**
     * 将JSON字符串反序列化为Map对象
     *
     * @param objectMapper JSON处理器
     * @param json         json字符串
     * @param valCls       值数据类型class
     * @param <R>          值类型
     * @return Map对象
     */
    @SneakyThrows({})
    public static <R> Map<String, R> fromJsonToMap(@Nonnull final ObjectMapper objectMapper, @Nonnull final String json,
                                                   @Nonnull final Class<R> valCls) {
        if (!Strings.isNullOrEmpty(json)) {
            final JavaType javaType = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, valCls);
            return objectMapper.readValue(json, javaType);
        }
        return Maps.newHashMap();
    }

    /**
     * 将JSON字符串反序列化为List-Map对象
     *
     * @param objectMapper JSON处理器
     * @param json         json字符串
     * @param valCls       值数据类型class
     * @param <R>          值类型
     * @return List-Map对象
     */
    @SneakyThrows({})
    public static <R> List<Map<String, R>> fromJsonToListMap(@Nonnull final ObjectMapper objectMapper,
                                                             @Nonnull final String json, @Nonnull final Class<R> valCls) {
        if (!Strings.isNullOrEmpty(json)) {
            final JavaType mapType = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, valCls);
            final JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, mapType);
            return objectMapper.readValue(json, listType);
        }
        return Lists.newArrayList();
    }

    /**
     * 将对象转换为Map对象
     *
     * @param objectMapper JSON处理器
     * @param data         数据
     * @param valCls       值数据类型class
     * @param <T>          数据类型
     * @param <R>          值类型
     * @return Map对象
     */
    @SneakyThrows({})
    public static <T, R> Map<String, R> toMap(@Nonnull final ObjectMapper objectMapper, @Nonnull final T data,
                                              @Nonnull final Class<R> valCls) {
        final String json = toJson(objectMapper, data);
        if (!Strings.isNullOrEmpty(json)) {
            return fromJsonToMap(objectMapper, json, valCls);
        }
        return Maps.newHashMap();
    }
}
