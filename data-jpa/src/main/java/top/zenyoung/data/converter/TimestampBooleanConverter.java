package top.zenyoung.data.converter;

import jakarta.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;

/**
 * 时间戳-布尔值转换器(用户软删除)
 *
 * @author young
 */
@Slf4j
public class TimestampBooleanConverter implements AttributeConverter<Boolean, Long> {
    @Override
    public Long convertToDatabaseColumn(final Boolean attribute) {
        return Boolean.TRUE.equals(attribute) ? System.currentTimeMillis() : 0L;
    }

    @Override
    public Boolean convertToEntityAttribute(final Long dbData) {
        return dbData != null && dbData > 0;
    }
}
