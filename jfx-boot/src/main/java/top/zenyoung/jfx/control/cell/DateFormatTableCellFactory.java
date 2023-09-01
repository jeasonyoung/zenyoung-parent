package top.zenyoung.jfx.control.cell;

import com.google.common.base.Strings;
import javafx.beans.NamedArg;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TableCell 时间数据格式化
 *
 * @param <S> 数据模型类型
 * @param <T> 字段数据类型
 */
@Getter
public class DateFormatTableCellFactory<S, T> extends BaseFormatTableCellFactory<S, T> {
    private final String format;

    public DateFormatTableCellFactory(@NamedArg("format") @Nonnull final String format) {
        this.format = format;
    }

    @Override
    protected String format(@Nonnull final T data) {
        if ((data instanceof Date) && !Strings.isNullOrEmpty(this.format)) {
            final DateFormat dateFormat = new SimpleDateFormat(this.format);
            final Date date = (Date) data;
            return dateFormat.format(date);
        }
        return data.toString();
    }
}
