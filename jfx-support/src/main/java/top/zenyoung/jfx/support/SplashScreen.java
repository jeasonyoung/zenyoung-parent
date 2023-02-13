package top.zenyoung.jfx.support;

import javafx.scene.Parent;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import top.zenyoung.common.util.JfxUtils;

import java.util.Objects;

/**
 * A default standard splash pane implementation Subclass it and override it's
 * methods to customize with your own behavior. Be aware that you can not use
 * Spring features here yet.
 *
 * @author Felix Roske
 * @author Andreas Jay
 */
public class SplashScreen {
    private final static String RES_PREFIX = "/top/zenyoung/jfx/support";

    /**
     * Override this to create your own splash pane parent node.
     *
     * @return A standard image
     */
    public Parent getParent() {
        final ImageView imageView = JfxUtils.fromResourceToImageView(getClass(), getImagePath());
        final ProgressBar splashProgressBar = new ProgressBar();
        if (Objects.nonNull(imageView)) {
            splashProgressBar.setPrefWidth(imageView.getImage().getWidth());
        }
        final VBox vbox = new VBox();
        vbox.getChildren().addAll(imageView, splashProgressBar);
        return vbox;
    }

    /**
     * Customize if the splash screen should be visible at all.
     *
     * @return true by default
     */
    public boolean visible() {
        return true;
    }

    /**
     * Use your own splash image instead of the default one.
     *
     * @return "/splash/javafx.png"
     */
    public String getImagePath() {
        return RES_PREFIX + "/splash/javafx.png";
    }
}
