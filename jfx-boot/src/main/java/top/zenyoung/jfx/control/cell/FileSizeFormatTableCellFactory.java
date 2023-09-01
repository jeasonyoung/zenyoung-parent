package top.zenyoung.jfx.control.cell;

import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;

/**
 * TableCell 文件大小格式化
 *
 * @param <S> 数据模型类型
 * @param <T> 字段数据类型
 */
public class FileSizeFormatTableCellFactory<S, T> extends BaseFormatTableCellFactory<S, T> {

    @Override
    protected String format(@Nonnull final T data) {
        if (data instanceof Long) {
            final Long val = (Long) data;
            return FileUtils.byteCountToDisplaySize(val);
        }
        return data.toString();
    }
}
