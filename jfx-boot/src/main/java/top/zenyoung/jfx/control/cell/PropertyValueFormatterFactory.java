package top.zenyoung.jfx.control.cell;

import com.sun.javafx.property.PropertyReference;
import javafx.beans.NamedArg;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

/**
 * 数据格式化-基类
 *
 * @author young
 */
@Slf4j
public abstract class PropertyValueFormatterFactory<S, T, R> implements Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<R>> {
    @Getter
    private final String property;
    private Class<?> columnClass;
    private String previousProperty;
    private PropertyReference<T> propertyRef;

    /**
     * 构造函数
     *
     * @param property 属性名
     */
    protected PropertyValueFormatterFactory(@NamedArg("property") @Nonnull final String property) {
        this.property = property;
    }

    @Override
    public ObservableValue<R> call(final TableColumn.CellDataFeatures<S, T> param) {
        return getCellDataReflectively(param.getValue());
    }

    private ObservableValue<R> getCellDataReflectively(final S rowData) {
        if (getProperty() == null || getProperty().isEmpty() || rowData == null) {
            return null;
        }
        try {
            // we attempt to cache the property reference here, as otherwise
            // performance suffers when working in large data models. For
            // a bit of reference, refer to RT-13937.
            if (columnClass == null || previousProperty == null ||
                    !columnClass.equals(rowData.getClass()) ||
                    !previousProperty.equals(getProperty())) {
                // create a new PropertyReference
                this.columnClass = rowData.getClass();
                this.previousProperty = getProperty();
                this.propertyRef = new PropertyReference<>(rowData.getClass(), getProperty());
            }

            if (propertyRef.hasProperty()) {
                final ReadOnlyProperty<T> val = propertyRef.getProperty(rowData);
                final R retVal = formatterHandler(val.getValue());
                return new ReadOnlyObjectWrapper<>(retVal);
            } else {
                final T value = propertyRef.get(rowData);
                final R retVal = formatterHandler(value);
                return new ReadOnlyObjectWrapper<>(retVal);
            }
        } catch (IllegalStateException e) {
            // log the warning and move on
            log.warn("Can not retrieve property '" + getProperty() +
                    "' in PropertyValueFactory: " + this +
                    " with provided class type: " + rowData.getClass(), e);
        }
        return null;
    }

    protected abstract R formatterHandler(final T data);
}
