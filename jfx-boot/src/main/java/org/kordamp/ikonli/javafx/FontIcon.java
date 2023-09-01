package org.kordamp.ikonli.javafx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.javafx.css.converters.PaintConverter;
import com.sun.javafx.css.converters.SizeConverter;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.IkonHandler;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * FontIcon
 *
 * @author young
 */
public class FontIcon extends Text implements Icon {
    private static final double EPSILON = 0.000001d;
    protected StyleableIntegerProperty iconSize;
    protected StyleableObjectProperty<Paint> iconColor;
    private StyleableObjectProperty<Ikon> iconCode;

    /**
     * 构造函数
     */
    public FontIcon() {
        getStyleClass().setAll("ikonli-font-icon");
        setIconSize(8);
        setIconColor(Color.BLACK);
        //font
        fontProperty().addListener((v, o, n) -> {
            final int size = (int) n.getSize();
            if (size != getIconSize()) {
                setIconSize(size);
            }
        });
        //fill
        fillProperty().addListener((v, o, n) -> {
            final Paint fill = getIconColor();
            if (!Objects.equals(fill, n)) {
                setIconColor(n);
            }
        });
        //icon code
        iconCodeProperty().addListener((v, o, n) -> {
            if (n != null) {
                final IkonHandler ikonHandler = IkonResolver.getInstance().resolve(n.getDescription());
                setStyle(normalizeStyle(getStyle(), "-fx-font-family", "'" + ikonHandler.getFontFamily() + "'"));
                final int code = n.getCode();
                if (code <= '\uFFFF') {
                    setText(String.valueOf((char) code));
                } else {
                    final char[] charPair = Character.toChars(code);
                    final String symbol = new String(charPair);
                    setText(symbol);
                }
            }
        });
    }

    public FontIcon(@Nonnull final String iconCode) {
        this();
        setIconLiteral(iconCode);
    }

    public FontIcon(@Nonnull final Ikon iconCode) {
        this();
        setIconCode(iconCode);
    }

    @Override
    public String toString() {
        final Ikon code = getIconCode();
        return (Objects.nonNull(code) ? code.getDescription() : "<undef>") + ":" + getIconSize();
    }

    @Override
    public IntegerProperty iconSizeProperty() {
        if (this.iconSize == null) {
            this.iconSize = new StyleableIntegerProperty(8) {
                @Override
                public Object getBean() {
                    return FontIcon.this;
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
            iconSize.addListener((v, o, n) -> {
                final Font font = FontIcon.this.getFont();
                if (Math.abs(font.getSize() - n.doubleValue()) >= EPSILON) {
                    FontIcon.this.setFont(Font.font(font.getFamily(), n.doubleValue()));
                    FontIcon.this.setStyle(normalizeStyle(getStyle(), "-fx-font-size", n.intValue() + "px"));
                }
            });
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
        if (this.iconColor == null) {
            this.iconColor = new StyleableObjectProperty<Paint>(Color.BLACK) {

                @Override
                public Object getBean() {
                    return FontIcon.this;
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
            iconColor.addListener((v, o, n) -> FontIcon.this.setFill(n));
        }
        return this.iconColor;
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

    public ObjectProperty<Ikon> iconCodeProperty() {
        if (this.iconCode == null) {
            this.iconCode = new StyleableObjectProperty<Ikon>() {

                @Override
                public Object getBean() {
                    return FontIcon.this;
                }

                @Override
                public String getName() {
                    return "iconCode";
                }

                @Override
                public CssMetaData<? extends Styleable, Ikon> getCssMetaData() {
                    return StyleableProperties.ICON_CODE;
                }
            };
            iconCode.addListener((v, o, n) -> {
                if (!iconCode.isBound()) {
                    FontIcon.this.setIconCode(n);
                }
            });
        }
        return this.iconCode;
    }

    public Ikon getIconCode() {
        return iconCodeProperty().get();
    }

    public void setIconCode(final Ikon iconCode) {
        Assert.notNull(iconCode, "Argument 'code' must not be null");
        iconCodeProperty().set(iconCode);
    }

    private String normalizeStyle(@Nonnull String style, @Nonnull final String key, @Nonnull final String value) {
        int start = style.indexOf(key);
        if (start != -1) {
            int end = style.indexOf(";", start);
            end = end >= start ? end : style.length() - 1;
            style = style.substring(0, start) + style.substring(end + 1);
        }
        return style + key + ": " + value + ";";
    }

    public String getIconLiteral() {
        final Ikon ikon = iconCodeProperty().get();
        return ikon != null ? ikon.getDescription() : null;
    }

    public void setIconLiteral(@Nonnull final String iconCode) {
        final String[] parts = iconCode.split(":");
        setIconCode(IkonResolver.getInstance().resolve(parts[0]).resolve(parts[0]));
        resolveSize(iconCode, parts);
        resolvePaint(iconCode, parts);
    }

    private void resolveSize(@Nonnull final String iconCode, @Nonnull final String[] parts) {
        if (parts.length > 1) {
            try {
                setIconSize(Integer.parseInt(parts[1]));
            } catch (NumberFormatException e) {
                throw invalidDescription(iconCode, e);
            }
        }
    }

    private void resolvePaint(String iconCode, String[] parts) {
        if (parts.length > 2) {
            Paint paint = resolvePaintValue(iconCode, parts[2]);
            if (paint != null) {
                setIconColor(paint);
            }
        }
    }

    private static Paint resolvePaintValue(@Nonnull final String iconCode, @Nonnull final String value) {
        try {
            return Color.valueOf(value);
        } catch (IllegalArgumentException e1) {
            try {
                return LinearGradient.valueOf(value);
            } catch (IllegalArgumentException e2) {
                try {
                    return RadialGradient.valueOf(value);
                } catch (IllegalArgumentException e3) {
                    throw invalidDescription(iconCode, e3);
                }
            }
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    public static FontIcon of(final Ikon ikon) {
        return of(ikon, 8, Color.BLACK);
    }

    public static FontIcon of(final Ikon ikon, final int iconSize) {
        return of(ikon, iconSize, Color.BLACK);
    }
    
    public static FontIcon of(final Ikon ikon, final Color iconColor) {
        return of(ikon, 8, iconColor);
    }

    public static FontIcon of(final Ikon iconCode, final int iconSize, final Color iconColor) {
        final FontIcon icon = new FontIcon();
        icon.setIconCode(iconCode);
        icon.setIconSize(iconSize);
        icon.setIconColor(iconColor);
        return icon;
    }

    public static IllegalArgumentException invalidDescription(final String description, final Exception e) {
        throw new IllegalArgumentException("Description " + description + " is not a valid icon description", e);
    }

    private static class StyleableProperties {
        private static final CssMetaData<FontIcon, Number> ICON_SIZE = new CssMetaData<FontIcon, Number>("-fx-icon-size", SizeConverter.getInstance(), 8) {
            @Override
            public boolean isSettable(final FontIcon icon) {
                return true;
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(final FontIcon icon) {
                return (StyleableIntegerProperty) icon.iconSizeProperty();
            }
        };
        private static final CssMetaData<FontIcon, Paint> ICON_COLOR = new CssMetaData<FontIcon, Paint>("-fx-icon-color", PaintConverter.getInstance(), Color.BLACK) {
            @Override
            public boolean isSettable(final FontIcon icon) {
                return true;
            }

            @Override
            public StyleableProperty<Paint> getStyleableProperty(final FontIcon icon) {
                return (StyleableObjectProperty<Paint>) icon.iconColorProperty();
            }
        };
        private static final CssMetaData<FontIcon, Ikon> ICON_CODE = new CssMetaData<FontIcon, Ikon>("-fx-icon-code", FontIconConverter.getInstance(), null) {
            @Override
            public boolean isSettable(final FontIcon icon) {
                return true;
            }

            @Override
            public StyleableProperty<Ikon> getStyleableProperty(final FontIcon icon) {
                return (StyleableObjectProperty<Ikon>) icon.iconCodeProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = Lists.newArrayList(Text.getClassCssMetaData());
            styleables.add(ICON_SIZE);
            styleables.add(ICON_COLOR);
            styleables.add(ICON_CODE);
            STYLEABLES = ImmutableList.copyOf(styleables);
        }
    }
}
