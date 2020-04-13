package top.zenyoung.common.converter;

import top.zenyoung.common.model.Status;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 状态-jpa类型转换
 *
 * @author yangyong
 * @version 1.0
 **/
@Converter
public class StatusConverter implements AttributeConverter<Status, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final Status status) {
        return status != null ? status.getVal() : null;
    }

    @Override
    public Status convertToEntityAttribute(final Integer val) {
        return Status.parse(val);
    }
}
