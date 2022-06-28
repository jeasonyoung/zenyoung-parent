package top.zenyoung.common.convert;

import javax.annotation.Nonnull;

/**
 * 类型转换接口
 *
 * @author young
 */
public interface TypeConvert<T> {
    /**
     * 执行类型转换
     *
     * @param fieldType 字段类型
     * @return 转换结果类型
     */
    T processTypeConvert(@Nonnull final String fieldType);
}
