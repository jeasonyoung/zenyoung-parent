package top.zenyoung.jfx.support;

import com.google.common.base.Strings;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * FXML View 基类
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/1/22 8:19 下午
 **/
@Slf4j
public abstract class AbstractFxmlView implements ApplicationContextAware {
    private final String fxmlRoot;
    private final FXMLView annotation;
    private final URL resource;

    private final ObjectProperty<Object> presenterProperty;

    private final ResourceBundle bundle;

    private ApplicationContext applicationContext;

    private FXMLLoader fxmlLoader;

    private boolean isPrimaryStageView = false;
    private Stage stage;
    private Modality currentStageModality;

    /**
     * 构造函数
     */
    public AbstractFxmlView() {
        //Set the root path to package path
        fxmlRoot = PropertyReaderHelper.determineFilePathFromPackageName(getClass());
        annotation = getFxmlAnnotation();
        resource = getUrlResource(annotation);

        presenterProperty = new SimpleObjectProperty<>();
        bundle = getResourceBundle(getBundleName());
    }

    private FXMLView getFxmlAnnotation() {
        final Class<? extends AbstractFxmlView> theClass = getClass();
        return theClass.getAnnotation(FXMLView.class);
    }

    private URL getUrlResource(final FXMLView annotation) {
        if (annotation != null && !Strings.isNullOrEmpty(annotation.value())) {
            return getClass().getResource(annotation.value());
        }
        return getClass().getResource(fxmlRoot + getConventionalName(".fxml"));
    }

    private String getConventionalName() {
        final String clazz = getClass().getSimpleName().toLowerCase();
        final String tag = "view";
        if (!clazz.endsWith(tag)) {
            return clazz;
        }
        return clazz.substring(0, clazz.lastIndexOf(tag));
    }

    private String getConventionalName(final String ending) {
        return getConventionalName() + ending;
    }

    private ResourceBundle getResourceBundle(final String name) {
        try {
            log.debug("Resource bundle: {}", name);
            return ResourceBundle.getBundle(name, new ResourceBundleControl(Charset.forName(annotation.encoding())));
        } catch (final MissingResourceException ex) {
            log.warn("getResourceBundle(name: {})-exp: {}", name, ex.getMessage());
            return null;
        }
    }

    private String getBundleName() {
        if (Strings.isNullOrEmpty(annotation.bundle())) {
            return getClass().getPackage().getName() + "." + getConventionalName();
        }
        return annotation.bundle();
    }

    @Override
    public void setApplicationContext(@Nonnull final ApplicationContext applicationContext) throws BeansException {
        if (this.applicationContext != null) {
            return;
        }
        this.applicationContext = applicationContext;
    }

    private Object createControllerForType(@Nonnull final Class<?> type) {
        return applicationContext.getBean(type);
    }

    private FXMLLoader loadSynchronously(@Nullable final URL resource, @Nonnull final ResourceBundle bundle) throws IllegalStateException {
        final FXMLLoader loader = new FXMLLoader(resource, bundle);
        loader.setControllerFactory(this::createControllerForType);
        try {
            loader.load();
        } catch (final IOException | IllegalStateException e) {
            log.error("loadSynchronously(resource: {},bundle: {})-exp: {}", resource, bundle, e.getMessage());
            throw new IllegalStateException("Cannot load " + getConventionalName(), e);
        }
        return loader;
    }

    private void ensureFxmlLoaderInitialized() {
        if (fxmlLoader != null) {
            return;
        }
        fxmlLoader = loadSynchronously(resource, bundle);
        presenterProperty.set(fxmlLoader.getController());
    }

    /**
     * Sets up the first view using the primary {@link Stage}
     */
    protected void initFirstView() {
        isPrimaryStageView = true;
        stage = GUIState.getStage();
        final Parent view = getView();
        if (view != null) {
            final Scene scene = view.getScene() != null ? view.getScene() : new Scene(view);
            stage.setScene(scene);
            GUIState.setScene(scene);
        }
    }

    public void hide() {
        if (stage != null) {
            stage.hide();
        }
    }

    /**
     * Shows the FxmlView instance being the child stage of the given {@link Window}
     *
     * @param window   The owner of the FxmlView instance
     * @param modality See {@code javafx.stage.Modality}.
     */
    public void showView(final Window window, final Modality modality) {
        if (!isPrimaryStageView && window != null && modality != null) {
            if (stage == null || currentStageModality != modality || !Objects.equals(stage.getOwner(), window)) {
                stage = createStage(modality);
                stage.initOwner(window);
            }
        }
        stage.show();
    }

    /**
     * Shows the FxmlView instance on a top level {@link Window}
     *
     * @param modality See {@code javafx.stage.Modality}.
     */
    public void showView(final Modality modality) {
        if (!isPrimaryStageView && modality != null) {
            if (stage == null || currentStageModality != modality) {
                stage = createStage(modality);
            }
        }
        stage.show();
    }

    /**
     * Shows the FxmlView instance being the child stage of the given {@link Window} and waits
     * to be closed before returning to the caller.
     *
     * @param window   The owner of the FxmlView instance
     * @param modality See {@code javafx.stage.Modality}.
     */
    public void showViewAndWait(final Window window, final Modality modality) {
        if (isPrimaryStageView) {
            showView(modality);
            return;
        }
        if (stage == null || currentStageModality != modality || !Objects.equals(stage.getOwner(), window)) {
            stage = createStage(modality);
            stage.initOwner(window);
        }
        stage.showAndWait();
    }

    /**
     * Shows the FxmlView instance on a top level {@link Window} and waits to be closed before
     * returning to the caller.
     *
     * @param modality See {@code javafx.stage.Modality}.
     */
    public void showViewAndWait(final Modality modality) {
        if (isPrimaryStageView) {
            showView(modality);
            return;
        }
        if (stage == null || currentStageModality != modality) {
            stage = createStage(modality);
        }
        stage.showAndWait();
    }

    private Stage createStage(final Modality modality) {
        currentStageModality = modality;
        final Stage stage = new Stage();
        stage.initModality(modality);
        stage.setTitle(annotation.title());
        stage.initStyle(StageStyle.valueOf(annotation.stageStyle().toUpperCase()));
        stage.getIcons().addAll(GUIState.getStage().getIcons());
        final Parent view = getView();
        if (view != null) {
            stage.setScene(view.getScene() != null ? getView().getScene() : new Scene(view));
        }
        return stage;
    }

    /**
     * Initializes the view by loading the FXML (if not happened yet) and
     * returns the top Node (parent) specified in the FXML file.
     *
     * @return the root view as determined from {@link FXMLLoader}.
     */
    public Parent getView() {
        ensureFxmlLoaderInitialized();
        final Parent parent = fxmlLoader.getRoot();
        if (parent != null) {
            addCssIfAvailable(parent);
        }
        return parent;
    }

    /**
     * Initializes the view synchronously and invokes the consumer with
     * the created parent Node within the FX UI thread.
     *
     * @param consumer an object interested in received the {@link Parent} as callback.
     */
    public void getView(@Nonnull final Consumer<Parent> consumer) {
        CompletableFuture.supplyAsync(this::getView, Platform::runLater).thenAccept(consumer);
    }

    /**
     * Scene Builder creates for each FXML document a root container. This
     * method omits the root container (e.g. AnchorPane) and gives you
     * the access to its first child.
     *
     * @return the first child of the AnchorPane or null if there are no
     * children available from this view.
     */
    public Node getViewWithoutRootContainer() {
        final ObservableList<Node> children = getView().getChildrenUnmodifiable();
        if (CollectionUtils.isEmpty(children)) {
            return null;
        }
        return children.listIterator().next();
    }

    private void addCssIfAvailable(@Nonnull final Parent parent) {
        // Read global css when available:
        final List<String> list = PropertyReaderHelper.get(applicationContext.getEnvironment(), "javafx.css");
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(css -> parent.getStylesheets().add(getClass().getResource(css).toExternalForm()));
        }
        addCssFromAnnotation(parent);
        final URL uri = getClass().getResource(fxmlRoot + getConventionalName(".css"));
        if (uri == null) {
            return;
        }
        final String uriToCss = uri.toExternalForm();
        if (!Strings.isNullOrEmpty(uriToCss)) {
            parent.getStylesheets().add(uriToCss);
        }
    }

    private void addCssFromAnnotation(@Nonnull final Parent parent) {
        if (annotation != null && annotation.css().length > 0) {
            for (final String cssFile : annotation.css()) {
                if (Strings.isNullOrEmpty(cssFile)) {
                    continue;
                }
                final URL uri = getClass().getResource(cssFile);
                if (uri != null) {
                    final String uriToCss = uri.toExternalForm();
                    parent.getStylesheets().add(uriToCss);
                    log.debug("css file added to parent: {}", cssFile);
                } else {
                    log.warn("referenced {} css file could not be located", cssFile);
                }
            }
        }
    }

    /**
     * Gets the default title for to be shown in a (un)modal window.
     *
     * @return the default title
     */
    protected String getDefaultTitle() {
        return annotation.title();
    }

    /**
     * Gets the default style for a (un)modal window.
     *
     * @return default style
     */
    protected StageStyle getDefaultStyle() {
        final String style = annotation.stageStyle();
        if (!Strings.isNullOrEmpty(style)) {
            return StageStyle.valueOf(style.toUpperCase());
        }
        return null;
    }

    /**
     * In case the view was not initialized yet, the conventional fxml
     * (airhacks.fxml for the AirhacksView and AirhacksPresenter) are loaded and
     * the specified presenter / controller is going to be constructed and returned.
     *
     * @return the corresponding controller / presenter (usually for a AirhacksView the AirhacksPresenter)
     */
    public Object getPresenter() {
        ensureFxmlLoaderInitialized();
        return presenterProperty.get();
    }

    /**
     * Does not initialize the view. Only registers the Consumer and waits until
     * the the view is going to be created / the method FXMLView#getView or FXMLView#getViewAsync invoked.
     *
     * @param presenterConsumer listener for the presenter construction
     */
    public void getPresenter(final Consumer<Object> presenterConsumer) {
        presenterProperty.addListener((observable, oldValue, newValue) -> presenterConsumer.accept(newValue));
    }

    /**
     * Gets the resource bundle.
     *
     * @return an existing resource bundle, or null
     */
    public Optional<ResourceBundle> getResourceBundle() {
        return Optional.ofNullable(bundle);
    }

    @Override
    public String toString() {
        return "AbstractFxmlView [presenterProperty=" + presenterProperty
                + ",bundle=" + bundle + ",resource=" + resource + ",fxmlRoot=" + fxmlRoot + "]";
    }
}