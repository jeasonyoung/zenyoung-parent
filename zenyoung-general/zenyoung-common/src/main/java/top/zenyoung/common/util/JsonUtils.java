package top.zenyoung.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Map;

/**
 * Json工具类
 *
 * @author yangyong
 * @version 1.0
 * date 2020/6/30 2:38 下午
 **/
public class JsonUtils {

    /**
     * 数据JSON化处理
     *
     * @param objectMapper JSON处理器
     * @param data         数据
     * @param <T>          数据类型
     * @return json字符串
     */
    @SneakyThrows
    public static <T> String toJson(@Nonnull final ObjectMapper objectMapper, @Nonnull final T data) {
        return objectMapper.writeValueAsString(data);
    }

    /**
     * 将JSON字符串反序列为对象
     *
     * @param objectMapper JSON处理器
     * @param json         json字符串
     * @param tClass       数据类型class
     * @param <R>          目标数据类型
     * @return 反序列为对象
     */
    @SneakyThrows
    public static <R> R fromJson(@Nonnull final ObjectMapper objectMapper, @Nonnull final String json, @Nonnull final Class<R> tClass) {
        if (!Strings.isNullOrEmpty(json)) {
            return objectMapper.readValue(json, tClass);
        }
        return null;
    }

    /**
     * 将Map转换为对象
     *
     * @param objectMapper JSON处理器
     * @param map          Map数据
     * @param tClass       目标数据类型Class
     * @param <R>          目标数据类型
     * @return 对象数据
     */
    public static <R> R fromMap(@Nonnull final ObjectMapper objectMapper, @Nonnull final Map<String, Serializable> map, @Nonnull final Class<R> tClass) {
        final String json = toJson(objectMapper, map);
        if (!Strings.isNullOrEmpty(json)) {
            return fromJson(objectMapper, json, tClass);
        }
        return null;
    }
}
