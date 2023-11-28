package top.zenyoung.data.jpa.converter;

import top.zenyoung.common.model.Status;

import javax.annotation.Nonnull;

/**
 * 状态枚举-类型转换器
 *
 * @author young
 */
public class StatusConverter extends BaseEnumConverter<Status> {

    @Override
    protected Status parse(@Nonnull final Integer val) {
        return Status.parse(val);
    }
}
