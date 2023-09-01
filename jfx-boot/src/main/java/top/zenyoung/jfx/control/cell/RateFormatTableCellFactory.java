package top.zenyoung.jfx.control.cell;

import javafx.beans.NamedArg;

import javax.annotation.Nonnull;

/**
 * TableCell 百分比格式化
 *
 * @param <S>
 * @param <T>
 */
public class RateFormatTableCellFactory<S, T> extends ValueFormatTableCellFactory<S, T> {

    /**
     * 构造函数
     *
     * @param format 格式化表达式
     */
    public RateFormatTableCellFactory(@NamedArg("format") @Nonnull final String format) {
        super(format);
    }

    @Override
    protected String format(@Nonnull final T data) {
        if (data instanceof Number) {
            final Number val = (Number) data;
            if (val.intValue() == 0) {
                return "-";
            }
        }
        return super.format(data);
    }
}
