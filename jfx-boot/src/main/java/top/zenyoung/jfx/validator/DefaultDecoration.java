package top.zenyoung.jfx.validator;

import com.google.common.base.Strings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.Objects;
import java.util.function.Function;

/**
 * DefaultDecoration provides default graphical decorations.
 *
 * @author r.lichtenberger@synedra.com
 */
public class DefaultDecoration {
    private static final String PREFIX = "/top/zenyoung/jfx/validator";

    private static final Image ERROR_IMAGE = fromResource(PREFIX + "/decoration-error.png");
    private static final Image WARNING_IMAGE = fromResource(PREFIX + "/decoration-warning.png");

    private static final String POPUP_SHADOW_EFFECT = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 5);";
    private static final String TOOLTIP_COMMON_EFFECTS = "-fx-font-weight: bold; -fx-padding: 5; -fx-border-width:1;";

    private static final String ERROR_TOOLTIP_EFFECT = POPUP_SHADOW_EFFECT + TOOLTIP_COMMON_EFFECTS
            + "-fx-background-color: FBEFEF; -fx-text-fill: cc0033; -fx-border-color:cc0033;";

    private static final String WARNING_TOOLTIP_EFFECT = POPUP_SHADOW_EFFECT + TOOLTIP_COMMON_EFFECTS
            + "-fx-background-color: FFFFCC; -fx-text-fill: CC9900; -fx-border-color: CC9900;";

    private static Function<ValidationMessage, Decoration> factory;

    private DefaultDecoration() {
    }

    public static Function<ValidationMessage, Decoration> getFactory() {
        if (factory == null) {
            factory = DefaultDecoration::createGraphicDecoration;
        }
        return factory;
    }

    public static void setFactory(@Nonnull final Function<ValidationMessage, Decoration> factory) {
        DefaultDecoration.factory = factory;
    }

    public static Decoration createStyleClassDecoration(@Nonnull final ValidationMessage message) {
        return new StyleClassDecoration("validatorfx-" + message.getSeverity().toString().toLowerCase());
    }

    public static Decoration createGraphicDecoration(@Nonnull final ValidationMessage message) {
        return new GraphicDecoration(createDecorationNode(message), Pos.TOP_LEFT);
    }

    private static Node createDecorationNode(@Nonnull final ValidationMessage message) {
        final Node graphic = Severity.ERROR == message.getSeverity() ? createErrorNode() : createWarningNode();
        graphic.getStyleClass().add("shadow_effect");
        final Label label = new Label();
        label.setGraphic(graphic);
        label.setTooltip(createTooltip(message));
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private static Tooltip createTooltip(@Nonnull final ValidationMessage message) {
        final Tooltip tooltip = new Tooltip(message.getText());
        tooltip.setOpacity(.9);
        tooltip.setAutoFix(true);
        tooltip.setStyle(Severity.ERROR == message.getSeverity() ? ERROR_TOOLTIP_EFFECT : WARNING_TOOLTIP_EFFECT);
        return tooltip;
    }

    private static Node createErrorNode() {
        return new ImageView(ERROR_IMAGE);
    }

    private static Node createWarningNode() {
        return new ImageView(WARNING_IMAGE);
    }

    private static Image fromResource(@Nonnull final String resourceName) {
        if (!Strings.isNullOrEmpty(resourceName)) {
            final URL url = DefaultDecoration.class.getResource(resourceName);
            if (Objects.nonNull(url)) {
                return new Image(url.toExternalForm());
            }
        }
        return null;
    }
}
