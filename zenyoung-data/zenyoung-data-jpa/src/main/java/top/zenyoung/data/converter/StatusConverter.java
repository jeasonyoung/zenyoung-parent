package top.zenyoung.data.converter;

import top.zenyoung.common.model.Status;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 状态-jpa类型转换
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/11/25 7:21 下午
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
