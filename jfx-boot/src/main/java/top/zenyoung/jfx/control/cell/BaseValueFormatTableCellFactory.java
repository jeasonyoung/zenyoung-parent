package top.zenyoung.jfx.control.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * 格式化TableCell值
 *
 * @param <S> 数据模型类型
 * @param <T> 数据值类型
 */
public abstract class BaseValueFormatTableCellFactory<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {

    @Override
    public TableCell<S, T> call(final TableColumn<S, T> param) {
        return new TableCell<S, T>() {
            @Override
            protected void updateItem(final T item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    return;
                }
                if (Objects.nonNull(item)) {
                    this.setText(format(item));
                }
            }
        };
    }

    protected abstract String format(@Nonnull final T data);
}
