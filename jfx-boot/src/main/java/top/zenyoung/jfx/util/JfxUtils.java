package top.zenyoung.jfx.util;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import top.zenyoung.jfx.Controller;
import top.zenyoung.jfx.Ui;

import javax.annotation.Nonnull;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Jfx 工具类
 *
 * @author young
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JfxUtils {
    private static final Map<Class<?>, Object> LOCKS = Maps.newConcurrentMap();
    private static final Cache<Class<? extends Controller>, Ui<? extends Controller>> UI_CACHE = CacheBuilder.newBuilder()
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
    public static <C extends Controller> Ui<C> buildUi(@Nonnull final Class<C> ctrClass, @Nonnull final String fxml) {
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
}