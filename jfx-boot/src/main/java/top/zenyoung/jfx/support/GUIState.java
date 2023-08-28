package top.zenyoung.jfx.support;

import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * The enum {@link GUIState} stores Scene and Stage objects as singletons in
 * this VM.
 *
 * @author Felix Roske
 * @author Andreas Jay
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GUIState {
    private static final GUIState state = new GUIState();
    /**
     * 场景
     */
    private Scene scene;
    /**
     * 舞台
     */
    private Stage stage;
    /**
     * 标题
     */
    private String title;
    /**
     * 系统托盘
     */
    private SystemTray systemTray;

    private static GUIState getInstance() {
        return state;
    }

    /**
     * 获取场景
     *
     * @return 场景
     */
    public static Scene getScene() {
        return getInstance().scene;
    }

    /**
     * 设置场景
     *
     * @param scene 场景
     */
    public static void setScene(@Nonnull final Scene scene) {
        getInstance().scene = scene;
    }

    /**
     * 获取舞台
     *
     * @return 舞台
     */
    public static Stage getStage() {
        return getInstance().stage;
    }

    /**
     * 设置舞台
     *
     * @param stage 舞台
     */
    public static void setStage(@Nonnull final Stage stage) {
        getInstance().stage = stage;
    }

    /**
     * 获取标题
     *
     * @return 标题
     */
    public static String getTitle() {
        return getInstance().title;
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public static void setTitle(@Nonnull final String title) {
        getInstance().title = title;
    }

    /**
     * 获取系统托盘
     *
     * @return 系统托盘
     */
    public static SystemTray getSystemTray() {
        return getInstance().systemTray;
    }

    /**
     * 设置系统托盘
     *
     * @param systemTray 系统托盘
     */
    public static void setSystemTray(@Nonnull final SystemTray systemTray) {
        getInstance().systemTray = systemTray;
    }
}
