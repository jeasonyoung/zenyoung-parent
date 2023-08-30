package org.kordamp.ikonli.javafx;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.Styleable;
import javafx.scene.paint.Paint;

/**
 * Icon-接口
 *
 * @author young
 */
public interface Icon extends Styleable {
    /**
     * 获取IconSize属性
     *
     * @return IconSize属性
     */
    IntegerProperty iconSizeProperty();

    /**
     * 设置IconSize
     *
     * @param size IconSize
     */
    void setIconSize(final int size);

    /**
     * 获取IconSize
     *
     * @return IconSize
     */
    int getIconSize();

    /**
     * 获取IconColor属性
     *
     * @return IconColor属性
     */
    ObjectProperty<Paint> iconColorProperty();

    /**
     * 设置IconColor
     *
     * @param paint IconColor对象
     */
    void setIconColor(final Paint paint);

    /**
     * 获取IconColor
     *
     * @return IconColor
     */
    Paint getIconColor();
}
