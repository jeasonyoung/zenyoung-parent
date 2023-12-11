package top.zenyoung.data.r2dbc.converter;

import top.zenyoung.common.model.Gender;

import javax.annotation.Nonnull;

/**
 * 性别枚举-类型转换器
 *
 * @author young
 */
public class GenderConverter extends BaseEnumConverter<Gender> {
    @Override
    protected Gender parse(@Nonnull final Integer val) {
        return Gender.parse(val);
    }
}
