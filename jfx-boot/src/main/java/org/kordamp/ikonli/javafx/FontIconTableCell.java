package org.kordamp.ikonli.javafx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.kordamp.ikonli.Ikon;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * FontIconTableCell
 *
 * @author young
 */
public class FontIconTableCell<S, T> extends TableCell<S, T> {
    private static final String ERROR_CONVERTER_NULL = "Argument 'converter' must not be null";
    private Subscription subscription;
    private final FontIcon icon;
    private final ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<>(this, "converter");

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn() {
        return param -> new FontIconTableCell<S, T>();
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(@Nonnull final StringConverter<T> converter) {
        return param -> new FontIconTableCell<S, T>(converter);
    }

    public FontIconTableCell() {
        this(new StringConverter<T>() {
            @Override
            public String toString(final T obj) {
                return Objects.nonNull(obj) ? String.valueOf(obj) : "";
            }

            @Override
            public T fromString(final String val) {
                // leave it as is for now
                return null;
            }
        });
    }

    public FontIconTableCell(final StringConverter<T> converter) {
        Assert.notNull(converter, ERROR_CONVERTER_NULL);
        this.getStyleClass().add("font-icon-table-cell");
        this.icon = new FontIcon();
        setConverter(converter);
    }

    public final ObjectProperty<StringConverter<T>> converterProperty() {
        return converter;
    }

    public final void setConverter(final StringConverter<T> converter) {
        Assert.notNull(converter, ERROR_CONVERTER_NULL);
        converterProperty().set(converter);
    }

    public final StringConverter<T> getConverter() {
        return converterProperty().get();
    }

    @Override
    protected void updateItem(final T item, final boolean empty) {
        super.updateItem(item, empty);
        if(empty){
            setGraphic(null);
        }else{
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
            final TableColumn<S, T> column = getTableColumn();
            final ObservableValue<T> observable = column == null ? null : column.getCellObservableValue(getIndex());
            if (observable != null) {
                final ChangeListener<T> listener = (v, o, n) -> setIconCode(n);
                observable.addListener(listener);
                subscription = () -> observable.removeListener(listener);
                setIconCode(observable.getValue());
            } else if (item != null) {
                setIconCode(item);
            }
            setGraphic(icon);
            setAlignment(Pos.CENTER);
        }
    }

    private void setIconCode(final T val) {
        if (val instanceof Ikon) {
            icon.setIconCode((Ikon) val);
        } else {
            icon.setIconLiteral(getConverter().toString(val));
        }
    }

    private interface Subscription {
        void unsubscribe();
    }
}
