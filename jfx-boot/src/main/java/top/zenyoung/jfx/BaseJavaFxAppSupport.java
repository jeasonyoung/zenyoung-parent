package top.zenyoung.jfx;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import top.zenyoung.jfx.support.PropertyReaderHelper;
import top.zenyoung.jfx.util.JfxUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The Class AbstractJavaFxApplicationSupport.
 *
 * @author Felix Roske
 */
@Slf4j
public abstract class BaseJavaFxAppSupport extends Application {
    private static String[] savedArgs = new String[0];
    private static Class<? extends BaseFxmlView> savedInitialView;
    private static SplashScreen splashScreen;

    private static ConfigurableApplicationContext applicationContext;
    private static Consumer<Throwable> errorAction = defaultErrorAction();

    private static final List<Image> ICONS = Lists.newArrayList();

    private final List<Image> defaultIcons = Lists.newArrayList();
    private final CompletableFuture<Runnable> splashIsShowing;

    protected BaseJavaFxAppSupport() {
        splashIsShowing = new CompletableFuture<>();
    }

    /**
     * 获取场景对象
     *
     * @return 场景对象
     */
    public static Scene getScene() {
        return GUIState.getScene();
    }

    /**
     * 获取舞台对象
     *
     * @return 舞台对象
     */
    public static Stage getStage() {
        return GUIState.getStage();
    }

    /**
     * 获取系统托盘
     *
     * @return 系统托盘
     */
    public static SystemTray getSystemTray() {
        return GUIState.getSystemTray();
    }

    private void loadIcons(@Nonnull final ConfigurableApplicationContext ctx) {
        try {
            final List<String> fsImages = PropertyReaderHelper.get(ctx.getEnvironment(), Constant.KEY_APPICONS);
            if (!fsImages.isEmpty()) {
                final Class<?> cls = getClass();
                fsImages.forEach(s -> {
                    final Image img = JfxUtils.fromResourceToImage(cls, s);
                    if (Objects.nonNull(img)) {
                        ICONS.add(img);
                    }
                });
            } else {
                // add factory images
                ICONS.addAll(defaultIcons);
            }
        } catch (Exception e) {
            log.error("Failed to load icons: ", e);
        }
    }

    @Override
    public final void init() {
        // Load in JavaFx Thread and reused by Completable Future, but should no be a big deal.
        defaultIcons.addAll(loadDefaultIcons());
        CompletableFuture.supplyAsync(() -> SpringApplication.run(this.getClass(), savedArgs))
                .whenComplete((ctx, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to load spring application context: ", throwable);
                        Platform.runLater(() -> errorAction.accept(throwable));
                    } else {
                        Platform.runLater(() -> {
                            loadIcons(ctx);
                            launchApplicationView(ctx);
                        });
                    }
                })
                .thenAcceptBothAsync(splashIsShowing, (ctx, closeSplash) -> Platform.runLater(closeSplash));
    }

    @Override
    public final void start(@Nonnull final Stage stage) {
        GUIState.setStage(stage);
        final Stage splashStage = new Stage(StageStyle.TRANSPARENT);
        if (splashScreen.visible()) {
            final Scene splashScene = new Scene(splashScreen.getParent(), Color.TRANSPARENT);
            splashStage.setScene(splashScene);
            splashStage.getIcons().addAll(defaultIcons);
            splashStage.initStyle(StageStyle.TRANSPARENT);
            beforeShowingSplash(splashStage);
            splashStage.show();
        }
        splashIsShowing.complete(() -> {
            showInitialView();
            if (splashScreen.visible()) {
                splashStage.hide();
                splashStage.setScene(null);
            }
        });
    }

    /**
     * Show initial view.
     */
    private void showInitialView() {
        final String stageStyle = applicationContext.getEnvironment().getProperty(Constant.KEY_STAGE_STYLE);
        if (stageStyle != null) {
            GUIState.getStage().initStyle(StageStyle.valueOf(stageStyle.toUpperCase()));
        } else {
            GUIState.getStage().initStyle(StageStyle.DECORATED);
        }
        beforeInitialView(GUIState.getStage(), applicationContext);
        showInitialView(savedInitialView);
    }

    /**
     * Launch application view.
     */
    private static void launchApplicationView(@Nonnull final ConfigurableApplicationContext ctx) {
        applicationContext = ctx;
    }

    /**
     * Show view.
     *
     * @param newView the new view
     */
    public static void showInitialView(@Nonnull final Class<? extends BaseFxmlView> newView) {
        try {
            final BaseFxmlView view = applicationContext.getBean(newView);
            view.initFirstView();
            applyEnvPropsToView();
            GUIState.getStage().getIcons().addAll(ICONS);
            GUIState.getStage().show();
        } catch (Exception e) {
            log.error("Failed to load application: ", e);
            errorAction.accept(e);
        }
    }

    protected static void setErrorAction(@Nonnull final Consumer<Throwable> callback) {
        errorAction = callback;
    }

    /**
     * Default error action that shows a message and closes the app.
     */
    private static Consumer<Throwable> defaultErrorAction() {
        return e -> {
            final Alert alert = new Alert(AlertType.ERROR, "Oops! An unrecoverable error occurred.\n" +
                    "Please contact your software vendor.\n\n" +
                    "The application will stop now.");
            alert.showAndWait().ifPresent(response -> Platform.exit());
        };
    }

    /**
     * Apply env props to view.
     */
    private static void applyEnvPropsToView() {
        final ConfigurableEnvironment env = applicationContext.getEnvironment();
        final Stage stage = GUIState.getStage();
        PropertyReaderHelper.setIfPresent(env, Constant.KEY_TITLE, String.class, stage::setTitle);
        PropertyReaderHelper.setIfPresent(env, Constant.KEY_STAGE_WIDTH, Double.class, stage::setWidth);
        PropertyReaderHelper.setIfPresent(env, Constant.KEY_STAGE_HEIGHT, Double.class, stage::setHeight);
        PropertyReaderHelper.setIfPresent(env, Constant.KEY_STAGE_RESIZABLE, Boolean.class, stage::setResizable);
    }

    @Override
    public final void stop() throws Exception {
        super.stop();
        if (applicationContext != null) {
            applicationContext.close();
        } // else: someone did it already
    }

    /**
     * Sets the title. Allows to overwrite values applied during construction at
     * a later time.
     *
     * @param title the new title
     */
    protected static void setTitle(@Nonnull final String title) {
        GUIState.getStage().setTitle(title);
    }

    public static void launch(@Nonnull final Class<? extends Application> appClass, @Nonnull final String[] args) {
        launch(appClass, BaseIndexFxmlView.class, args);
    }

    /**
     * Launch app.
     *
     * @param appClass the app class
     * @param view     the view
     * @param args     the args
     */
    public static void launch(@Nonnull final Class<? extends Application> appClass,
                              @Nonnull final Class<? extends BaseFxmlView> view,
                              @Nonnull final String[] args) {
        launch(appClass, view, new SplashScreen(), args);
    }

    /**
     * Launch app.
     *
     * @param appClass the app class
     * @param view     the view
     * @param splash   the splash screen
     * @param args     the args
     */
    public static void launch(@Nonnull final Class<? extends Application> appClass,
                              @Nonnull final Class<? extends BaseFxmlView> view,
                              @Nullable final SplashScreen splash, @Nonnull final String[] args) {
        savedInitialView = view;
        savedArgs = args;
        if (Objects.nonNull(splash)) {
            splashScreen = splash;
        } else {
            splashScreen = new SplashScreen();
        }
        if (SystemTray.isSupported()) {
            GUIState.setSystemTray(SystemTray.getSystemTray());
        }
        Application.launch(appClass, args);
    }

    /**
     * Gets called after full initialization of Spring application context
     * and JavaFX platform right before the initial view is shown.
     * Override this method as a hook to add special code for your app. Especially meant to
     * add AWT code to add a system tray icon and behavior by calling
     * GUIState.getSystemTray() and modifying it accordingly.
     * <p>
     * By default noop.
     *
     * @param stage can be used to customize the stage before being displayed
     * @param ctx   represents spring ctx where you can loog for beans.
     */
    protected void beforeInitialView(@Nonnull final Stage stage, @Nonnull final ConfigurableApplicationContext ctx) {
    }

    /**
     * 启动屏幕开始前方法
     *
     * @param splashStage SplashScreen
     */
    protected void beforeShowingSplash(@Nonnull final Stage splashStage) {

    }

    public Collection<Image> loadDefaultIcons() {
        final String prefix = "/top/zenyoung/jfx/support/icons";
        final List<String> images = Lists.newArrayList(
                "/gear_16x16.png",
                "/gear_24x24.png",
                "/gear_36x36.png",
                "/gear_42x42.png",
                "/gear_64x64.png"
        );
        final Class<?> cls = getClass();
        return images.stream()
                .filter(path -> !Strings.isNullOrEmpty(path))
                .map(path -> {
                    try {
                        return JfxUtils.fromResourceToImage(cls, prefix + path);
                    } catch (Exception e) {
                        log.warn("loadDefaultIcons(path: {})-exp: {}", path, e.getMessage());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
