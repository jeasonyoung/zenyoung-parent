package top.zenyoung.jpa.converter;

import top.zenyoung.common.model.Gender;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 性别-jpa类型转换
 *
 * @author young
 */
@Converter
public class GenderConverter implements AttributeConverter<Gender, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final Gender gender) {
        return (gender == null ? Gender.None : gender).getVal();
    }

    @Override
    public Gender convertToEntityAttribute(final Integer val) {
        return Gender.parse(val);
    }
}
