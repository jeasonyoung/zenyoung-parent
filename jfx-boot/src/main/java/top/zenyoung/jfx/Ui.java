package top.zenyoung.jfx;

import com.google.common.base.Strings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import top.zenyoung.jfx.model.DragResize;
import top.zenyoung.jfx.model.ViewSize;
import top.zenyoung.jfx.util.JfxUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Ui对象
 *
 * @param <C> 控制器类型
 * @author young
 */
public class Ui<C extends BaseFxmlController> {
    private final AtomicBoolean refEnableResize = new AtomicBoolean(false);
    private final AtomicReference<DragResize> refResize = new AtomicReference<>(null);
    private static final String FXML_EXT = ".fxml";
    private final FXMLLoader fxmlLoader;

    @Getter
    private final Parent root;

    @Getter
    private final C controller;

    private Scene scene = null;

    @SneakyThrows
    public Ui(@Nonnull final String fxml) {
        final URL url = getFxmlUrl(fxml);
        this.fxmlLoader = new FXMLLoader();
        this.fxmlLoader.setLocation(url);
        this.root = this.fxmlLoader.load(url.openStream());
        this.controller = this.fxmlLoader.getController();
        this.controller.setRoot(this.root);
    }

    private URL getFxmlUrl(@Nonnull final String fxml) {
        final String ext = FilenameUtils.getExtension(fxml);
        return getClass().getResource(Strings.isNullOrEmpty(ext) ? fxml + FXML_EXT : fxml);
    }

    public void requestFocus() {
        if (this.root != null) {
            this.root.requestFocus();
        }
    }

    public Scene getScene(@Nullable final Stage stage, @Nonnull final ViewSize viewSize) {
        if (this.scene == null) {
            synchronized (this) {
                //初始化场景
                this.scene = new Scene(this.root, viewSize.getWidth(), viewSize.getHeight());
                //添加样式
                this.scene.getStylesheets().add(JfxUtils.getBootstrapFXStylesheet());
            }
        }
        //添加界面拖动效果
        if (stage != null) {
            JfxUtils.addDragHandler(stage, this.scene, refResize.get());
        }
        //返回场景
        return this.scene;
    }

    public void enableResize(@Nonnull final Stage stage) {
        if (!refEnableResize.get()) {
            //添加界面拉伸效果
            JfxUtils.addResizeDrawHandler(stage, this.scene, refResize::set);
            //标记
            refEnableResize.set(true);
        }
    }

    public void reInitializable() {
        if (fxmlLoader != null && controller != null) {
            //初始化
            controller.initialize(fxmlLoader.getLocation(), fxmlLoader.getResources());
        }
    }
}
