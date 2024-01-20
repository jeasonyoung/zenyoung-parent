package top.zenyoung.data.r2dbc.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * 日期转换
 *
 * @author young
 */
@ReadingConverter
public class ZonedDateTimeToDateConverter implements Converter<ZonedDateTime, Date> {

    @Override
    public Date convert(@Nonnull final ZonedDateTime source) {
        return Date.from(source.toInstant());
    }
}
