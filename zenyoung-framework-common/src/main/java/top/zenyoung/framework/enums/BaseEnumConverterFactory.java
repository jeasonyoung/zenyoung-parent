package top.zenyoung.framework.enums;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import top.zenyoung.common.model.EnumValue;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * 参数请求枚举处理工厂基类
 *
 * @author young
 */
public abstract class BaseEnumConverterFactory implements ConverterFactory<String, EnumValue> {
    private static final Map<Class<? extends EnumValue>, Converter<String, BaseEnumConverter<? extends EnumValue>>> ENUM_CACHES = new WeakHashMap<>();

    ///TODO: 通用枚举请求参数处理
}
