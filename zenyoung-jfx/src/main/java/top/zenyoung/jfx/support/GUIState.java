package top.zenyoung.jfx.support;

import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

/**
 * The enum {@link GUIState} stores Scene and Stage objects as singletons in this VM.
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/1/23 5:55 下午
 **/
public enum GUIState {
    /**
     * 枚举实例
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
     * 宿主服务
     */
    @Getter
    @Setter
    private static HostServices hostServices;

    /**
     * 系统图标
     */
    @Getter
    @Setter
    private static SystemTray systemTray;
}
