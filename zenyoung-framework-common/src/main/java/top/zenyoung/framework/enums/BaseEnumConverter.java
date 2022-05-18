package top.zenyoung.framework.enums;

import org.springframework.core.convert.converter.Converter;
import top.zenyoung.common.model.EnumValue;

/**
 * 参数请求枚举转换器
 *
 * @author young
 */
public abstract class BaseEnumConverter<T extends EnumValue> implements Converter<String, T> {

}
