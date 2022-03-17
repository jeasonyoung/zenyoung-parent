package top.zenyoung.web.controller;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Map;

/**
 * 参数序列化处理
 *
 * @author young
 */
public interface ParamHandler {
    /**
     * 参数集合序列化
     *
     * @param params 参数集合
     * @return 序列化数据
     */
    String serialize(@Nonnull final Map<String, Serializable> params);

    /**
     * 参数反序列化为对象
     *
     * @param json       序列化数据
     * @param paramClass 参数类型
     * @param <T>        参数泛型
     * @return 参数对象
     */
    <T extends Serializable> T deserialize(@Nonnull final String json, @Nonnull final Class<T> paramClass);

    /**
     * 参数验证处理
     *
     * @param req 请求参数对象
     * @param <T> 请求参数类型
     * @throws Exception 异常消息
     */
    <T extends Serializable> void paramValidator(@Nonnull final T req) throws Exception;
}
