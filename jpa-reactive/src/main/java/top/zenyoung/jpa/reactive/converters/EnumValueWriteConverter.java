package top.zenyoung.jpa.reactive.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nonnull;

@WritingConverter
public class EnumValueWriteConverter implements Converter<EnumValue, Integer> {
    @Override
    public Integer convert(@Nonnull final EnumValue source) {
        return source.getVal();
    }
}
