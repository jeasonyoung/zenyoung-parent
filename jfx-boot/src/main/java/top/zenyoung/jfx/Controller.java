package top.zenyoung.jfx;

import javafx.scene.Node;
import top.zenyoung.jfx.util.ThreadUtils;

import javax.annotation.Nonnull;

/**
 * 控制器接口
 *
 * @author young
 */
public interface Controller {
    /**
     * 设置控制器root
     *
     * @param root root对象
     */
    default void setRoot(@Nonnull final Node root) {

    }

    /**
     * 初始化之后执行
     */
    default void initializeAfter() {
    }

    /**
     * 主线程执行处理
     *
     * @param handler 执行处理器
     */
    default void mainRun(@Nonnull final Runnable handler) {
        ThreadUtils.mainRun(handler);
    }
}
