package top.zenyoung.data.r2dbc.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import javax.annotation.Nonnull;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * 日期转换
 */
@WritingConverter
public class DateWriterConverter implements Converter<Date, ZonedDateTime> {

    @Override
    public ZonedDateTime convert(@Nonnull final Date source) {
        return ZonedDateTime.ofInstant(source.toInstant(), ZoneId.systemDefault());
    }
}
