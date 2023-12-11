package top.zenyoung.data.r2dbc.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nonnull;

/**
 * 枚举写入转换器
 *
 * @author young
 */
@WritingConverter
public class EnumWriterConverter implements Converter<EnumValue, Integer> {
    @Override
    public Integer convert(@Nonnull final EnumValue source) {
        return source.getVal();
    }
}
