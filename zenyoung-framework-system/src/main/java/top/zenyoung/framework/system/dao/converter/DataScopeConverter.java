package top.zenyoung.framework.system.dao.converter;

import top.zenyoung.framework.system.model.DataScope;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 数据权限范围-jpa类型转换
 *
 * @author young
 */
@Converter
public class DataScopeConverter implements AttributeConverter<DataScope, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final DataScope type) {
        return type == null ? null : type.getVal();
    }

    @Override
    public DataScope convertToEntityAttribute(final Integer val) {
        return DataScope.parse(val);
    }
}
