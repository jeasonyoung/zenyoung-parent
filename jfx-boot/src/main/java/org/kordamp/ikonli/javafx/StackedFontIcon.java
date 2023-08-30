package org.kordamp.ikonli.javafx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.javafx.css.converters.PaintConverter;
import com.sun.javafx.css.converters.SizeConverter;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.css.*;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.Ikon;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * StackedFontIcon
 *
 * @author young
 */
public class StackedFontIcon extends StackPane implements Icon {
    private static final String KEY_STACKED_FONT_ICON_SIZE = StackedFontIcon.class.getName() + ".iconSize";

    private StyleableIntegerProperty iconSize;
    private StyleableObjectProperty<Paint> iconColor;
    private double[] iconSizes = new double[0];

    private final ChangeListener<Number> iconSizeChangeListener = (v, o, n) -> setIconSizeOnChildren(n.intValue());
    private final ChangeListener<Paint> iconColorChangeListener = (v, o, n) -> setIconColorOnChildren(n);

    public StackedFontIcon() {
        getStyleClass().setAll("stacked-ikonli-font-icon");
        final String propertiesListenerKey = StackedFontIcon.class.getName() + "-" + System.identityHashCode(this);
        getChildren().addListener((ListChangeListener<Node>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    final int size = c.getTo() - c.getFrom();
                    // grow iconSizes by size
                    iconSizes = Arrays.copyOf(iconSizes, iconSizes.length + size);
                    // apply 1.0 [from..to]
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        iconSizes[i] = getIconSize(c.getList().get(i));
                    }
                    for (Node node : c.getAddedSubList()) {
                        node.getProperties().put(propertiesListenerKey, new NodeSizeListener(node));
                    }
                } else if (c.wasRemoved()) {
                    final int size = c.getTo() - c.getFrom();
                    // shrink iconSizes by size
                    final double[] newIconSizes = new double[iconSizes.length - size];
                    // copy [0..from]
                    int index = 0;
                    for (int i = 0; i < c.getFrom(); i++) {
                        newIconSizes[index++] = iconSizes[i];
                    }
                    // copy [to..-1]
                    for (int i = c.getTo(); i < iconSizes.length; i++) {
                        newIconSizes[index++] = iconSizes[i];
                    }
                    iconSizes = newIconSizes;
                    for (Node node : c.getRemoved()) {
                        node.getProperties().remove(propertiesListenerKey);
                    }
                } else if (c.wasPermutated()) {
                    final double[] newIconSizes = Arrays.copyOf(iconSizes, iconSizes.length);
                    for (int i = c.getFrom(); i <= c.getTo(); i++) {
                        newIconSizes[i] = c.getPermutation(i);
                    }
                    iconSizes = newIconSizes;
                }
            }
        });
    }

    @Override
    public IntegerProperty iconSizeProperty() {
        if (this.iconSize == null) {
            this.iconSize = new StyleableIntegerProperty(16) {
                @Override
                public Object getBean() {
                    return StackedFontIcon.this;
                }

                @Override
                public String getName() {
                    return "iconSize";
                }

                @Override
                public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                    return StyleableProperties.ICON_SIZE;
                }
            };
            iconSize.addListener(iconSizeChangeListener);
        }
        return this.iconSize;
    }

    @Override
    public void setIconSize(final int size) {
        if (size <= 0) {
            throw new IllegalStateException("Argument 'size' must be greater than zero.");
        }
        iconSizeProperty().set(size);
    }

    @Override
    public int getIconSize() {
        return iconSizeProperty().get();
    }

    @Override
    public ObjectProperty<Paint> iconColorProperty() {
        if (iconColor == null) {
            iconColor = new StyleableObjectProperty<Paint>(Color.BLACK) {
                @Override
                public Object getBean() {
                    return StackedFontIcon.this;
                }

                @Override
                public String getName() {
                    return "iconColor";
                }

                @Override
                public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
                    return StyleableProperties.ICON_COLOR;
                }
            };
            iconColor.addListener(iconColorChangeListener);
        }
        return iconColor;
    }

    @Override
    public void setIconColor(final Paint paint) {
        Assert.notNull(paint, "Argument 'paint' must not be null");
        iconColorProperty().set(paint);
    }

    @Override
    public Paint getIconColor() {
        return iconColorProperty().get();
    }

    public void setIconCodes(@Nonnull final Ikon... iconCodes) {
        getChildren().clear();
        initializeSizesIfNeeded(iconCodes);
        updateIconCodes(iconCodes);
    }

    public void setIconCodeLiterals(@Nonnull final String... iconCodes) {
        getChildren().clear();
        final Ikon[] codes = new Ikon[iconCodes.length];
        for (int i = 0; i < iconCodes.length; i++) {
            codes[i] = IkonResolver.getInstance().resolve(iconCodes[i]).resolve(iconCodes[i]);
        }
        initializeSizesIfNeeded(iconCodes);
        updateIconCodes(codes);
    }

    public void setIconSizes(@Nonnull final double... iconSizes) {
        this.iconSizes = iconSizes;
        setIconSizeOnChildren(getIconSize());
    }

    public void setColors(@Nonnull final Paint... iconColors) {
        int i = 0;
        for (Node node : getChildren()) {
            if (node instanceof Icon) {
                ((Icon) node).setIconColor(iconColors[i++]);
            }
        }
    }

    private void initializeSizesIfNeeded(@Nonnull final Object[] array) {
        if (iconSizes.length == 0 || iconSizes.length != array.length) {
            iconSizes = new double[array.length];
            Arrays.fill(iconSizes, 1d);
        }
    }

    private void updateIconCodes(@Nonnull final Ikon[] iconCodes) {
        for (int index = 0; index < iconCodes.length; index++) {
            getChildren().add(createFontIcon(iconCodes[index], index));
        }
    }

    private FontIcon createFontIcon(@Nonnull final Ikon iconCode, final int index) {
        final FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(getIconSize());
        icon.setIconColor(getIconColor());
        final int size = icon.getIconSize();
        applySizeToIcon(size, icon, index);
        return icon;
    }

    public static void setIconSize(final Node icon, final double size) {
        if (Objects.nonNull(icon) && size >= 0d && size <= 1.0d) {
            icon.getProperties().put(KEY_STACKED_FONT_ICON_SIZE, size);
        }
    }

    public static double getIconSize(final Node icon) {
        if (Objects.nonNull(icon)) {
            final Object val = icon.getProperties().get(KEY_STACKED_FONT_ICON_SIZE);
            if (val instanceof Number) {
                return ((Number) val).doubleValue();
            }
        }
        return 1.0d;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return StackedFontIcon.getClassCssMetaData();
    }

    private void setIconSizeOnChildren(final int size) {
        int i = 0;
        for (Node node : getChildren()) {
            if (node instanceof Icon) {
                applySizeToIcon(size, (Icon) node, i++);
            }
        }
    }

    private void applySizeToIcon(final int size, final Icon icon, final int index) {
        final double childPercentageSize = iconSizes[index];
        final double newSize = size * childPercentageSize;
        icon.setIconSize((int) newSize);
    }

    private void setIconColorOnChildren(@Nonnull final Paint color) {
        for (Node node : getChildren()) {
            if (node instanceof Icon) {
                ((Icon) node).setIconColor(color);
            }
        }
    }

    @RequiredArgsConstructor
    private class NodeSizeListener implements MapChangeListener<Object, Object> {
        private final Node node;

        @Override
        public void onChanged(final Change<?, ?> change) {
            final String key = String.valueOf(change.getKey());
            if (KEY_STACKED_FONT_ICON_SIZE.equals(key)) {
                final int size = getChildren().size();
                for (int i = 0; i < size; i++) {
                    if (node == getChildren().get(i)) {
                        double value = 0;
                        final Object valueAdded = change.getValueAdded();
                        if (valueAdded instanceof Number) {
                            value = ((Number) valueAdded).doubleValue();
                        } else {
                            value = Double.parseDouble(String.valueOf(valueAdded));
                        }
                        iconSizes[i] = value;
                        return;
                    }
                }
            }
        }
    }

    private static class StyleableProperties {
        private static final CssMetaData<StackedFontIcon, Number> ICON_SIZE = new CssMetaData<StackedFontIcon, Number>(
                "-fx-icon-size", SizeConverter.getInstance(), 16.0
        ) {
            @Override
            public boolean isSettable(StackedFontIcon fontIcon) {
                return true;
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(StackedFontIcon icon) {
                return (StyleableIntegerProperty) icon.iconSizeProperty();
            }
        };

        private static final CssMetaData<StackedFontIcon, Paint> ICON_COLOR = new CssMetaData<StackedFontIcon, Paint>(
                "-fx-icon-color", PaintConverter.getInstance(), Color.BLACK
        ) {
            @Override
            public boolean isSettable(StackedFontIcon node) {
                return true;
            }

            @Override
            public StyleableProperty<Paint> getStyleableProperty(StackedFontIcon icon) {
                return (StyleableObjectProperty<Paint>) icon.iconColorProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = Lists.newArrayList(StackPane.getClassCssMetaData());
            styleables.add(ICON_SIZE);
            styleables.add(ICON_COLOR);
            STYLEABLES = ImmutableList.copyOf(styleables);
        }
    }
}
