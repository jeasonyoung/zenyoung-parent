package top.zenyoung.jfx.support;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.CollectionUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * The Class AbstractJavaFxApplicationSupport.
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/1/26 7:51 下午
 **/
@Slf4j
public abstract class AbstractJavaFxApplicationSupport extends Application {
    private static final List<Image> DEFAULT_ICONS = new ArrayList<>() {
        {
            add(new Image(getClass().getResource("/icons/gear_16x16.png").toExternalForm()));
            add(new Image(getClass().getResource("/icons/gear_24x24.png").toExternalForm()));
            add(new Image(getClass().getResource("/icons/gear_36x36.png").toExternalForm()));
            add(new Image(getClass().getResource("/icons/gear_42x42.png").toExternalForm()));
            add(new Image(getClass().getResource("/icons/gear_64x64.png").toExternalForm()));
        }
    };
    private static final Consumer<Throwable> ERROR_ACTION = e -> {
        log.error("Oops! An unrecoverable error occurred.\nPlease contact your software vendor.\n\nThe application will stop now.", e);
        final Alert alert = new Alert(Alert.AlertType.ERROR, "Oops! An unrecoverable error occurred.\n" +
                "Please contact your software vendor.\n\n" +
                "The application will stop now.");
        alert.showAndWait().ifPresent(resp -> Platform.exit());
    };

    private static String[] savedArgs = new String[0];
    private static Class<? extends AbstractFxmlView> savedInitialView;
    private static SplashScreen splashScreen;
    private static ConfigurableApplicationContext applicationContext;
    private static List<Image> icons = Lists.newArrayList();

    private final CompletableFuture<Runnable> splashIsShowing;

    protected AbstractJavaFxApplicationSupport() {
        splashIsShowing = new CompletableFuture<>();
    }

    public static Stage getStage() {
        return GUIState.getStage();
    }

    public static Scene getScene() {
        return GUIState.getScene();
    }

    public static HostServices getAppHostServices() {
        return GUIState.getHostServices();
    }

    public static SystemTray getSystemTray() {
        return GUIState.getSystemTray();
    }

    private void loadIcons(final ConfigurableApplicationContext ctx) {
        try {
            final List<String> fsImages = PropertyReaderHelper.get(ctx.getEnvironment(), Constant.KEY_APPICONS);
            if (!CollectionUtils.isEmpty(fsImages)) {
                fsImages.forEach(fsImage -> {
                    final Image img = new Image(getClass().getResource(fsImage).toExternalForm());
                    icons.add(img);
                });
            } else {
                icons.addAll(DEFAULT_ICONS);
            }
        } catch (Throwable ex) {
            log.error("Failed to load icons:", ex);
        }
    }

    @Override
    public void init() {
        CompletableFuture.supplyAsync(() -> SpringApplication.run(getClass(), savedArgs))
                .whenComplete((ctx, e) -> {
                    if (e != null) {
                        log.error("Failed to load spring application context:", e);
                        Platform.runLater(() -> ERROR_ACTION.accept(e));
                        return;
                    }
                    Platform.runLater(() -> {
                        loadIcons(ctx);
                        AbstractJavaFxApplicationSupport.applicationContext = ctx;
                    });
                })
                .thenAcceptBothAsync(splashIsShowing, (ctx, closeSplash) -> Platform.runLater(closeSplash));
    }

    @Override
    public void start(final Stage stage) throws Exception {
        GUIState.setStage(stage);
        GUIState.setHostServices(getHostServices());
        final Stage splashStage = new Stage(StageStyle.TRANSPARENT);
        if (splashScreen != null && splashScreen.visible()) {
            final Scene splashScene = new Scene(splashScreen.getParent(), Color.TRANSPARENT);
            splashStage.setScene(splashScene);
            splashStage.getIcons().addAll(DEFAULT_ICONS);
            splashStage.initStyle(StageStyle.TRANSPARENT);
            beforeShowingSplash(splashStage);
            splashStage.show();
        }
        splashIsShowing.complete(() -> {
            showInitialView();
            if (splashScreen != null && splashScreen.visible()) {
                splashStage.hide();
                splashStage.setScene(null);
            }
        });
    }

    private void showInitialView() {
        if (applicationContext != null) {
            final String stageStyle = applicationContext.getEnvironment().getProperty(Constant.KEY_STAGE_STYLE);
            if (!Strings.isNullOrEmpty(stageStyle)) {
                GUIState.getStage().initStyle(StageStyle.valueOf(stageStyle.toUpperCase()));
            } else {
                GUIState.getStage().initStyle(StageStyle.DECORATED);
            }
            beforeInitialView(GUIState.getStage(), applicationContext);
            showInitialView(savedInitialView);
        }
    }

    public static void showInitialView(final Class<? extends AbstractFxmlView> newView) {
        try {
            final AbstractFxmlView view = applicationContext.getBean(newView);
            view.initFirstView();
            //title
            PropertyReaderHelper.setIfPresent(applicationContext.getEnvironment(), Constant.KEY_TITLE, String.class, GUIState.getStage()::setTitle);
            //width
            PropertyReaderHelper.setIfPresent(applicationContext.getEnvironment(), Constant.KEY_STAGE_WIDTH, Double.class, GUIState.getStage()::setWidth);
            //height
            PropertyReaderHelper.setIfPresent(applicationContext.getEnvironment(), Constant.KEY_STAGE_HEIGHT, Double.class, GUIState.getStage()::setHeight);
            //resizeable
            PropertyReaderHelper.setIfPresent(applicationContext.getEnvironment(), Constant.KEY_STAGE_RESIZABLE, Boolean.class, GUIState.getStage()::setResizable);
            //icons
            GUIState.getStage().getIcons().addAll(icons);
            //show
            GUIState.getStage().show();
        } catch (Throwable e) {
            log.error("Failed to load application:", e);
            ERROR_ACTION.accept(e);
        }
    }

    /**
     * Gets called after full initialization of Spring application context
     * and JavaFX platform right before the initial view is shown.
     * Override this method as a hook to add special code for your app. Especially meant to
     * add AWT code to add a system tray icon and behavior by calling
     * GUIState.getSystemTray() and modifying it accordingly.
     *
     * @param stage can be used to customize the stage before being displayed
     * @param ctx   represents spring ctx where you can loog for beans.
     */
    protected void beforeInitialView(final Stage stage, final ConfigurableApplicationContext ctx) {

    }

    protected void beforeShowingSplash(final Stage splashStage) {

    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (applicationContext != null) {
            applicationContext.close();
        }
    }

    /**
     * Sets the title. Allows to overwrite values applied during construction at a later time.
     *
     * @param title the new title
     */
    protected static void setTitle(final String title) {
        GUIState.getStage().setTitle(title);
    }

    /**
     * launch app.
     *
     * @param appClass the app class
     * @param view     the view
     * @param args     the args
     */
    public static void launch(final Class<? extends Application> appClass, final Class<? extends AbstractFxmlView> view, final String[] args) {
        launch(appClass, view, new SplashScreen(), args);
    }

    /**
     * launch app.
     *
     * @param appClass the app class
     * @param view     the view
     * @param splash   the splash screen
     * @param args     the args
     */
    public static void launch(final Class<? extends Application> appClass, final Class<? extends AbstractFxmlView> view, final SplashScreen splash, final String[] args) {
        savedInitialView = view;
        savedArgs = args;
        splashScreen = splash != null ? splash : new SplashScreen();
        if (SystemTray.isSupported()) {
            GUIState.setSystemTray(SystemTray.getSystemTray());
        }
        Application.launch(appClass, args);
    }
}
