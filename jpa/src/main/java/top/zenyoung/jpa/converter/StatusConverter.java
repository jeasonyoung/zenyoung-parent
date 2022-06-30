package top.zenyoung.jpa.converter;

import top.zenyoung.common.model.Status;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 状态-jpa类型转换
 *
 * @author young
 **/
@Converter
public class StatusConverter implements AttributeConverter<Status, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final Status attr) {
        return attr != null ? attr.getVal() : null;
    }

    @Override
    public Status convertToEntityAttribute(final Integer data) {
        return Status.parse(data);
    }
}
