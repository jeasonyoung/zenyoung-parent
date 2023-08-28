package top.zenyoung.jfx.util;

import com.google.common.base.Strings;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.Objects;

/**
 * Jfx 工具类
 *
 * @author young
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JfxUtils {
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
