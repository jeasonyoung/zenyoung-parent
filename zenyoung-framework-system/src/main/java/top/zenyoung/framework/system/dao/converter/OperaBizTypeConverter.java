package top.zenyoung.framework.system.dao.converter;

import top.zenyoung.framework.common.OperaBizType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 业务操作记录-jpa类型转换
 *
 * @author young
 */
@Converter
public class OperaBizTypeConverter implements AttributeConverter<OperaBizType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final OperaBizType type) {
        return type == null ? null : type.getVal();
    }

    @Override
    public OperaBizType convertToEntityAttribute(final Integer val) {
        return OperaBizType.parse(val);
    }
}
