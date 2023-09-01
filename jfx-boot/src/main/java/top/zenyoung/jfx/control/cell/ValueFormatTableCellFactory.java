package top.zenyoung.jfx.control.cell;

import com.google.common.base.Strings;
import javafx.beans.NamedArg;
import lombok.Getter;

import javax.annotation.Nonnull;

/**
 * TableCell 通用数据格式化
 *
 * @param <S> 数据模型类型
 * @param <T> 字段数据类型
 * @author young
 */
@Getter
public class ValueFormatTableCellFactory<S, T> extends BaseFormatTableCellFactory<S, T> {
    private static final String PREFIX = "%";
    private final String format;

    public ValueFormatTableCellFactory(@NamedArg("format") @Nonnull final String format) {
        this.format = format;
    }

    @Override
    protected String format(@Nonnull final T data) {
        if (!Strings.isNullOrEmpty(this.format)) {
            final String f = this.format.startsWith(PREFIX) ? this.format : PREFIX + this.format;
            return String.format(f, data);
        }
        return data.toString();
    }
}
