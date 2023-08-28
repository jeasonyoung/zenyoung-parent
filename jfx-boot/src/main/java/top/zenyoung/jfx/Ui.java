package top.zenyoung.jfx;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.AtomicDouble;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
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
public class Ui<C extends Controller> {
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
        this.controller.initializeAfter();
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
            this.addDragHandler(stage);
        }
        //返回场景
        return this.scene;
    }

    public void enableResize(@Nonnull final Stage stage) {
        if (!refEnableResize.get()) {
            //添加界面拉伸效果
            addResizeDrawHandler(stage);
            //标记
            refEnableResize.set(true);
        }
    }

    private void addDragHandler(@Nullable final Stage stage) {
        if (scene == null || stage == null) {
            return;
        }
        //拖动事件处理
        final AtomicDouble refX1 = new AtomicDouble(0);
        final AtomicDouble refY1 = new AtomicDouble(0);
        final AtomicDouble refStageX = new AtomicDouble(0);
        final AtomicDouble refStageY = new AtomicDouble(0);
        scene.setOnDragEntered(null);
        scene.setOnMousePressed(e -> {
            //按下鼠标后，记录当前鼠标当前的坐标
            refX1.set(e.getScreenX());
            refY1.set(e.getScreenY());
            refStageX.set(stage.getX());
            refStageY.set(stage.getY());
        });
        scene.setOnMouseDragged(e -> {
            //检查是否支持resize
            final DragResize resize;
            if (refEnableResize.get() && (resize = refResize.get()) != null && resize.isResize()) {
                draggedResizeHandler(stage, resize, e);
            } else {
                //计算
                stage.setX(refStageX.doubleValue() + e.getScreenX() - refX1.doubleValue());
                stage.setY(refStageY.doubleValue() + e.getScreenY() - refY1.doubleValue());
            }
        });
    }

    private void draggedResizeHandler(@Nonnull final Stage stage, @Nonnull final DragResize resize, @Nonnull final MouseEvent e) {
        final double x = e.getSceneX(), y = e.getSceneY();
        //保存窗口改变后的x、y坐标和宽度、高度，用于预判是否会小于最小宽度、最小高度
        final double nextX = stage.getX(), nextY = stage.getY();
        double nextW = stage.getWidth(), nextH = stage.getHeight();
        if (resize.isRight() || resize.isBottomRight()) {
            //所有右边调整窗口状态
            nextW = x;
        }
        if (resize.isBottomRight() || resize.isBottom()) {
            //所有下边调整窗口状态
            nextH = y;
        }
        if (nextW <= DragResize.MIN_WIDTH) {
            //窗口改变后的宽度小于最小宽度，则宽度调整到最小宽度
            nextW = DragResize.MIN_WIDTH;
        }
        if (nextH <= DragResize.MIN_HEIGHT) {
            //窗口改变后的高度小于最小高度，则高度调整到最小高度
            nextH = DragResize.MIN_HEIGHT;
        }
        //最后统一改变窗口的x、y坐标和宽度、高度，可以防止刷新频繁出现的屏闪情况
        stage.setX(nextX);
        stage.setY(nextY);
        stage.setWidth(nextW);
        stage.setHeight(nextH);
    }

    private void addResizeDrawHandler(@Nullable final Stage stage) {
        if (scene == null || stage == null) {
            return;
        }
        final DragResize resize = new DragResize();
        //添加鼠标事件
        scene.setOnMouseMoved(e -> {
            e.consume();
            final double x = e.getSceneX(), y = e.getSceneY();
            final double w = stage.getWidth(), h = stage.getHeight();
            //鼠标光标初始为默认类型，若未进入调整窗口状态，保持默认类型
            Cursor cursor = Cursor.DEFAULT;
            //先将所有调整窗口状态重置
            resize.clear();
            //判断鼠标位置
            if (y >= h - DragResize.RESIZE_WIDTH) {
                if (x <= DragResize.RESIZE_WIDTH) {
                    //左下角调整窗口状态
                    resize.clear();
                } else if (x >= w - DragResize.RESIZE_WIDTH) {
                    //右下角调整窗口状态
                    resize.setBottomRight(true);
                    cursor = Cursor.SE_RESIZE;
                } else {
                    //下边界调整窗口状态
                    resize.setBottom(true);
                    cursor = Cursor.S_RESIZE;
                }
            } else if (x >= w - DragResize.RESIZE_WIDTH) {
                //右边界调整窗口状态
                resize.setRight(true);
                cursor = Cursor.E_RESIZE;
            }
            refResize.set(resize);
            //最后改变鼠标光标
            scene.setCursor(cursor);
        });
    }

    public void reInitializable() {
        if (fxmlLoader != null && controller != null) {
            //初始化
            if ((controller instanceof Initializable)) {
                ((Initializable) controller).initialize(fxmlLoader.getLocation(), fxmlLoader.getResources());
            }
            //初始化之后执行
            controller.initializeAfter();
        }
    }

    @Data
    private static class DragResize {
        /**
         * 判定是否为调整窗口状态的范围与边界距离
         */
        public static final int RESIZE_WIDTH = 5;
        /**
         * 窗口最小宽度
         */
        public static final int MIN_WIDTH = 300;
        /**
         * 窗口最小高度
         */
        public static final int MIN_HEIGHT = 250;

        /**
         * 是否处于右边调整状态
         */
        private boolean right;
        /**
         * 是否处于右下角调整状态
         */
        private boolean bottomRight;
        /**
         * 是否处于下边调整状态
         */
        private boolean bottom;

        public void clear() {
            right = bottomRight = bottom = false;
        }

        public boolean isResize() {
            return right || bottomRight || bottom;
        }
    }
}
