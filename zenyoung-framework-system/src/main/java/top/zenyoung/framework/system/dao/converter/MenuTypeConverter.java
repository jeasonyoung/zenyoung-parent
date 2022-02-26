package top.zenyoung.framework.system.dao.converter;

import top.zenyoung.framework.system.model.MenuType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 菜单类型-jpa类型转换
 *
 * @author young
 */
@Converter
public class MenuTypeConverter implements AttributeConverter<MenuType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final MenuType type) {
        return type == null ? null : type.getVal();
    }

    @Override
    public MenuType convertToEntityAttribute(final Integer val) {
        return MenuType.parse(val);
    }
}
