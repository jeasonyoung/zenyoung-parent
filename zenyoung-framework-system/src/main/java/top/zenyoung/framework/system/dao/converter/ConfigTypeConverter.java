package top.zenyoung.framework.system.dao.converter;

import top.zenyoung.framework.system.model.ConfigType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 配置类型转换
 *
 * @author young
 */
@Converter
public class ConfigTypeConverter implements AttributeConverter<ConfigType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final ConfigType type) {
        return type == null ? null : type.getVal();
    }

    @Override
    public ConfigType convertToEntityAttribute(final Integer val) {
        return ConfigType.parse(val);
    }
}
