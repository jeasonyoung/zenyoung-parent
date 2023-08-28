package top.zenyoung.common.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Icon工具类
 *
 * @author young
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IconUtils {

    /**
     * 构建FontIcon图片
     *
     * @param ikon      Ikon对象
     * @param iconSize  icon尺寸
     * @param iconColor icon颜色
     * @return icon图片
     */
    public static BufferedImage buildFontIconImage(@Nonnull final Ikon ikon,
                                                   @Nullable final Integer iconSize,
                                                   @Nullable final Color iconColor) {
        FontIcon fontIcon;
        if (Objects.nonNull(iconSize) && Objects.nonNull(iconColor)) {
            if (iconSize >= 0) {
                fontIcon = FontIcon.of(ikon, iconSize, iconColor);
            } else {
                fontIcon = FontIcon.of(ikon, iconColor);
            }
        } else if (Objects.isNull(iconSize)) {
            fontIcon = FontIcon.of(ikon, iconColor);
        } else {
            fontIcon = FontIcon.of(ikon, iconSize);
        }
        return buildFontIconImage(fontIcon);
    }

    /**
     * 构建FontIcon图片
     *
     * @param icon FontIcon
     * @return icon图片
     */
    public static BufferedImage buildFontIconImage(@Nonnull final FontIcon icon) {
        final FontIconUi ui = FontIconUi.of(icon);
        return ui.buildImage();
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class FontIconUi {
        private static final SnapshotParameters PARAMS;

        static {
            PARAMS = new SnapshotParameters();
            // 设置透明背景或其他颜色
            PARAMS.setFill(Color.TRANSPARENT);
        }

        private final FontIcon icon;

        private Parent getParent() {
            final VBox vbox = new VBox();
            vbox.getChildren().add(icon);
            return vbox;
        }

        private void initScene() {
            final Stage stage = new Stage(StageStyle.TRANSPARENT);
            final Scene scene = new Scene(getParent(), Color.TRANSPARENT);
            stage.setScene(scene);
        }

        public BufferedImage buildImage() {
            if (Objects.isNull(icon.getScene())) {
                initScene();
            }
            final WritableImage wi = icon.snapshot(PARAMS, null);
            if (Objects.nonNull(wi)) {
                return SwingFXUtils.fromFXImage(wi, null);
            }
            return null;
        }
    }
}
