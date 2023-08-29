package top.zenyoung.jfx.util;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicDouble;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import top.zenyoung.jfx.BaseFxmlController;
import top.zenyoung.jfx.Ui;
import top.zenyoung.jfx.model.DragResize;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Jfx 工具类
 *
 * @author young
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JfxUtils {
    private static final Map<Class<?>, Object> LOCKS = Maps.newConcurrentMap();
    private static final Cache<Class<? extends BaseFxmlController>, Ui<? extends BaseFxmlController>> UI_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .build();

    /**
     * 构建UI对象
     *
     * @param ctrClass 控制器Class
     * @param fxml     xml文件
     * @param <C>      控制器类型
     * @return UI对象
     */
    @SuppressWarnings({"unchecked"})
    public static <C extends BaseFxmlController> Ui<C> buildUi(@Nonnull final Class<C> ctrClass, @Nonnull final String fxml) {
        Ui<C> ui = (Ui<C>) UI_CACHE.getIfPresent(ctrClass);
        if (ui != null) {
            ui.reInitializable();
            return ui;
        }
        synchronized (LOCKS.computeIfAbsent(ctrClass, k -> new Object())) {
            try {
                ui = new Ui<>(fxml);
                UI_CACHE.put(ctrClass, ui);
            } finally {
                LOCKS.remove(ctrClass);
            }
        }
        return ui;
    }

    /**
     * 主函数处理
     *
     * @param handler 业务处理器
     */
    public static void mainRun(@Nonnull final Runnable handler) {
        //检查是否为UI线程
        if (Platform.isFxApplicationThread()) {
            handler.run();
            return;
        }
        //UI线程处理
        Platform.runLater(handler);
    }

    /**
     * 获取资源文件路径
     *
     * @param cls  Class对象
     * @param path 资源文件路径
     * @return 资源文件路径
     */
    public static String fromResource(@Nonnull final Class<?> cls, @Nonnull final String path) {
        if (!Strings.isNullOrEmpty(path)) {
            final URL url = cls.getResource(path);
            if (Objects.nonNull(url)) {
                return url.toExternalForm();
            }
        }
        return null;
    }

    /**
     * 从资源中加载ImageView对象
     *
     * @param cls  Class对象
     * @param path 资源文件路径
     * @return ImageView对象
     */
    public static ImageView fromResourceToImageView(@Nonnull final Class<?> cls, @Nonnull final String path) {
        final String uri = fromResource(cls, path);
        if (!Strings.isNullOrEmpty(uri)) {
            return new ImageView(uri);
        }
        return null;
    }

    /**
     * 从资源中加载Image对象
     *
     * @param cls  Class对象
     * @param path 资源文件路径
     * @return Image对象
     */
    public static Image fromResourceToImage(@Nonnull final Class<?> cls, @Nonnull final String path) {
        final String uri = fromResource(cls, path);
        if (!Strings.isNullOrEmpty(uri)) {
            return new Image(uri);
        }
        return null;
    }

    /**
     * 获取Bootstrap全局样式文件
     *
     * @return 样式文件
     */
    public static String getBootstrapFXStylesheet() {
        final String css = "/org/kordamp/bootstrapfx/bootstrapfx.css";
        return fromResource(Panel.class, css);
    }

    /**
     * 窗口拖拽移动
     *
     * @param stage      舞台对象
     * @param scene      场景对象
     * @param dragResize 是否进行重置尺寸
     */
    public static void addDragHandler(@Nonnull final Stage stage, @Nonnull final Scene scene, @Nullable final DragResize dragResize) {
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
            if (Objects.nonNull(resize = dragResize) && resize.isResize()) {
                draggedResizeHandler(stage, resize, e);
            } else {
                //计算
                stage.setX(refStageX.doubleValue() + e.getScreenX() - refX1.doubleValue());
                stage.setY(refStageY.doubleValue() + e.getScreenY() - refY1.doubleValue());
            }
        });
    }

    private static void draggedResizeHandler(@Nonnull final Stage stage, @Nonnull final DragResize resize, @Nonnull final MouseEvent e) {
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

    /**
     * 添加界面拉伸效果
     *
     * @param stage             舞台对象
     * @param scene             场景对象
     * @param dragResizeHandler 拖曳重置尺寸
     */
    public static void addResizeDrawHandler(@Nonnull final Stage stage, @Nonnull final Scene scene,
                                            @Nullable final Consumer<DragResize> dragResizeHandler) {
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
            if (Objects.nonNull(dragResizeHandler)) {
                dragResizeHandler.accept(resize);
            }
            //最后改变鼠标光标
            scene.setCursor(cursor);
        });
    }
}
