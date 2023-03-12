package top.zenyoung.jfx.support;

import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

/**
 * The enum {@link GUIState} stores Scene and Stage objects as singletons in
 * this VM.
 *
 * @author Felix Roske
 * @author Andreas Jay
 */
@Getter
public enum GUIState {
    /**
     * 实例
     */
    INSTANCE;

    /**
     * 场景
     */
    @Getter
    @Setter
    private static Scene scene;

    /**
     * 舞台
     */
    @Getter
    @Setter
    private static Stage stage;

    /**
     * 标题
     */
    @Getter
    @Setter
    private static String title;

    /**
     * 系统托盘
     */
    @Getter
    @Setter
    private static SystemTray systemTray;
}
