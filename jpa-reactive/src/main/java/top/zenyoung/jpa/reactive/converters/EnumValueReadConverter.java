package top.zenyoung.jpa.reactive.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nonnull;

@ReadingConverter
public class EnumValueReadConverter<T extends EnumValue> implements Converter<Integer, T> {

    @Override
    public T convert(@Nonnull final Integer source) {

        return null;
    }
}
