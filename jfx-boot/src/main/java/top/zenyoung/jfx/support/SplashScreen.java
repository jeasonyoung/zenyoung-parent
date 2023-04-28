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
     * 宽度
     */
    public Double getPrefWidth() {
        return null;
    }

    /**
     * 获取高度
     *
     * @return 高度
     */
    public Double getPrefHeight() {
        return null;
    }

    /**
     * Override this to create your own splash pane parent node.
     *
     * @return A standard image
     */
    public Parent getParent() {
        final VBox vbox = new VBox();
        final ImageView imageView = JfxUtils.fromResourceToImageView(getClass(), getImagePath());
        if (Objects.nonNull(imageView)) {
            final ProgressBar splashProgressBar = new ProgressBar();
            //宽度
            final Double prefWidth;
            if (Objects.nonNull(prefWidth = getPrefWidth()) && prefWidth > 0) {
                imageView.setFitWidth(prefWidth);
                splashProgressBar.setPrefWidth(prefWidth);
            } else {
                splashProgressBar.setPrefWidth(imageView.getImage().getWidth());
            }
            //高度
            final Double prefHeight;
            if (Objects.nonNull(prefHeight = getPrefHeight()) && prefHeight > 0) {
                imageView.setFitHeight(prefHeight);
            }
            //添加
            vbox.getChildren().addAll(imageView, splashProgressBar);
        }
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
