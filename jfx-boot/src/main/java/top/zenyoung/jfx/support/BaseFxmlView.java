package top.zenyoung.jfx.support;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.util.JfxUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.ResourceBundle.getBundle;

/**
 * Base class for fxml-based view classes.
 * <p>
 * It is derived from Adam Bien's
 * <a href="http://afterburner.adam-bien.com/">afterburner.fx</a> project.
 * <p>
 * {@link BaseFxmlView} is a stripped down version of <a href=
 * "https://github.com/AdamBien/afterburner.fx/blob/02f25fdde9629fcce50ea8ace5dec4f802958c8d/src/main/java/com/airhacks/afterburner/views/FXMLView.java"
 * >FXMLView</a> that provides DI for Java FX Controllers via Spring.
 * </p>
 * <p>
 * Supports annotation driven creation of FXML based view beans with {@link FXMLView}
 * </p>
 *
 * @author Thomas Darimont
 * @author Felix Roske
 * @author Andreas Jay
 */
@Slf4j
public abstract class BaseFxmlView implements ApplicationContextAware {
    private final String fxmlRoot;
    private final FXMLView annotation;
    private final URL resource;
    private final ObjectProperty<Object> presenterProperty;
    private final ResourceBundle bundle;

    private Stage stage;
    private FXMLLoader fxmlLoader;
    private Modality currentStageModality;
    private boolean isPrimaryStageView = false;
    private ApplicationContext applicationContext;

    /**
     * Instantiates a new abstract fxml view.
     */
    public BaseFxmlView() {
        log.debug("AbstractFxmlView construction");
        // Set the root path to package path
        this.fxmlRoot = PropertyReaderHelper.determineFilePathFromPackageName(getClass());
        this.annotation = getFxmlAnnotation();
        this.resource = getUrlResource(annotation);
        this.presenterProperty = new SimpleObjectProperty<>();
        this.bundle = getResourceBundle(getBundleName());
    }

    /**
     * Gets the URL resource. This will be derived from applied annotation value
     * or from naming convention.
     *
     * @param annotation the annotation as defined by inheriting class.
     * @return the URL resource
     */
    private URL getUrlResource(@Nullable final FXMLView annotation) {
        final String value;
        if (Objects.nonNull(annotation) && !Strings.isNullOrEmpty(value = annotation.value())) {
            return getClass().getResource(value);
        } else {
            return getClass().getResource(getFxmlPath());
        }
    }

    /**
     * Gets the {@link FXMLView} annotation from inheriting class.
     *
     * @return the FXML annotation
     */
    private FXMLView getFxmlAnnotation() {
        final Class<? extends BaseFxmlView> theClass = this.getClass();
        return theClass.getAnnotation(FXMLView.class);
    }

    /**
     * Creates the controller for type.
     *
     * @param type the type
     * @return the object
     */
    private Object createControllerForType(@Nonnull final Class<?> type) {
        return applicationContext.getBean(type);
    }

    @Override
    public void setApplicationContext(@Nonnull final ApplicationContext context) throws BeansException {
        if (Objects.nonNull(this.applicationContext)) {
            return;
        }
        this.applicationContext = context;
    }

    /**
     * Load synchronously.
     *
     * @param resource the resource
     * @param bundle   the bundle
     * @return the FXML loader
     * @throws IllegalStateException the illegal state exception
     */
    private FXMLLoader loadSynchronously(@Nonnull final URL resource, @Nullable final ResourceBundle bundle) throws IllegalStateException {
        final FXMLLoader loader = new FXMLLoader(resource, bundle);
        loader.setControllerFactory(this::createControllerForType);
        try {
            loader.load();
        } catch (final IOException | IllegalStateException e) {
            throw new IllegalStateException("Cannot load " + getConventionalName(), e);
        }
        return loader;
    }

    /**
     * Ensure fxml loader initialized.
     */
    private void ensureFxmlLoaderInitialized() {
        if (Objects.nonNull(this.fxmlLoader)) {
            return;
        }
        fxmlLoader = loadSynchronously(resource, bundle);
        presenterProperty.set(fxmlLoader.getController());
    }

    /**
     * Sets up the first view using the primary {@link Stage}
     */
    protected void initFirstView() {
        this.isPrimaryStageView = true;
        this.stage = GUIState.getStage();
        final Scene scene = getView().getScene() != null ? getView().getScene() : new Scene(getView());
        this.stage.setScene(scene);
        GUIState.setScene(scene);
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
    public void showView(@Nonnull final Window window, @Nonnull final Modality modality) {
        if (!isPrimaryStageView &&
                (stage == null || currentStageModality != modality || !Objects.equals(stage.getOwner(), window))) {
            stage = createStage(modality);
            stage.initOwner(window);
        }
        stage.show();
    }

    /**
     * Shows the FxmlView instance on a top level {@link Window}
     *
     * @param modality See {@code javafx.stage.Modality}.
     */
    public void showView(@Nonnull final Modality modality) {
        if (!isPrimaryStageView && (stage == null || currentStageModality != modality)) {
            stage = createStage(modality);
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
    public void showViewAndWait(@Nonnull final Window window, @Nonnull final Modality modality) {
        if (isPrimaryStageView) {
            // this modality will be ignored anyway
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
    public void showViewAndWait(@Nonnull final Modality modality) {
        if (isPrimaryStageView) {
            // this modality will be ignored anyway
            showView(modality);
            return;
        }
        if (stage == null || currentStageModality != modality) {
            stage = createStage(modality);
        }
        stage.showAndWait();
    }

    private Stage createStage(@Nonnull final Modality modality) {
        currentStageModality = modality;
        final Stage stage = new Stage();
        stage.initModality(modality);
        stage.setTitle(getDefaultTitle());
        stage.initStyle(getDefaultStyle());
        final List<Image> primaryStageIcons = GUIState.getStage().getIcons();
        stage.getIcons().addAll(primaryStageIcons);
        final Scene scene = getView().getScene() != null ? getView().getScene() : new Scene(getView());
        stage.setScene(scene);
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
        //添加默认样式
        addCssIfAvailable(parent);
        //返回对象
        return parent;
    }

    /**
     * Initializes the view synchronously and invokes the consumer with the
     * created parent Node within the FX UI thread.
     *
     * @param consumer - an object interested in received the {@link Parent} as
     *                 callback
     */
    public void getView(@Nonnull final Consumer<Parent> consumer) {
        CompletableFuture.supplyAsync(this::getView, Platform::runLater)
                .thenAccept(consumer);
    }

    /**
     * Scene Builder creates for each FXML document a root container. This
     * method omits the root container (e.g. {@link AnchorPane}) and gives you
     * the access to its first child.
     *
     * @return the first child of the {@link AnchorPane} or null if there are no
     * children available from this view.
     */
    public Node getViewWithoutRootContainer() {
        final ObservableList<Node> children = getView()
                .getChildrenUnmodifiable();
        if (children.isEmpty()) {
            return null;
        }
        return children.listIterator().next();
    }

    /**
     * Adds the CSS if available.
     *
     * @param parent the parent
     */
    private void addCssIfAvailable(@Nonnull final Parent parent) {
        final List<String> globals = Lists.newArrayList();
        // Read global css when available:
        final List<String> list = PropertyReaderHelper.get(applicationContext.getEnvironment(), Constant.KEY_CSS);
        if (!CollectionUtils.isEmpty(list)) {
            final Class<?> cls = getClass();
            final String sep = ";";
            globals.addAll(list.stream()
                    .map(css -> {
                        if (!Strings.isNullOrEmpty(css)) {
                            if (css.contains(sep)) {
                                return Splitter.on(sep).omitEmptyStrings().trimResults().splitToList(css);
                            }
                            return Lists.newArrayList(css);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .map(css -> JfxUtils.fromResource(cls, css))
                    .filter(path -> !Strings.isNullOrEmpty(path))
                    .distinct()
                    .collect(Collectors.toList())
            );
        } else {
            final String css = JfxUtils.getBootstrapCss();
            if (!Strings.isNullOrEmpty(css)) {
                globals.add(css);
            }
        }
        if (!CollectionUtils.isEmpty(globals)) {
            parent.getStylesheets().addAll(globals);
        }
        //注解样式加载
        addCssFromAnnotation(parent);
        //默认样式文件
        final String uriToCss = JfxUtils.fromResource(getClass(), getStyleSheetName());
        if (!Strings.isNullOrEmpty(uriToCss)) {
            parent.getStylesheets().add(uriToCss);
        }
    }

    /**
     * Adds the CSS from annotation to parent.
     *
     * @param parent the parent
     */
    private void addCssFromAnnotation(@Nonnull final Parent parent) {
        final String[] css;
        if (Objects.nonNull(annotation) && Objects.nonNull(css = annotation.css()) && css.length > 0) {
            final Class<?> cls = getClass();
            final List<String> annSheets = Stream.of(css)
                    .map(f -> JfxUtils.fromResource(cls, f))
                    .filter(path -> !Strings.isNullOrEmpty(path))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(annSheets)) {
                parent.getStylesheets().addAll(annSheets);
            }
        }
    }

    /**
     * Gets the default title for to be shown in a (un)modal window.
     * @return default title
     */
    protected String getDefaultTitle() {
        return annotation.title();
    }

    /**
     * Gets the default style for a (un)modal window.
     */
    protected StageStyle getDefaultStyle() {
        final String style = annotation.stageStyle();
        return StageStyle.valueOf(style.toUpperCase());
    }

    /**
     * Gets the style sheet name.
     *
     * @return the style sheet name
     */
    private String getStyleSheetName() {
        return fxmlRoot + getConventionalName(".css");
    }

    /**
     * In case the view was not initialized yet, the conventional fxml
     * (airhacks.fxml for the AirhacksView and AirhacksPresenter) are loaded and
     * the specified presenter / controller is going to be constructed and
     * returned.
     *
     * @return the corresponding controller / presenter (usually for a
     * AirhacksView the AirhacksPresenter)
     */
    public Object getPresenter() {
        ensureFxmlLoaderInitialized();
        return presenterProperty.get();
    }

    /**
     * Does not initialize the view. Only registers the Consumer and waits until
     * the the view is going to be created / the method FXMLView#getView or
     * FXMLView#getViewAsync invoked.
     *
     * @param presenterConsumer listener for the presenter construction
     */
    public void getPresenter(@Nonnull final Consumer<Object> presenterConsumer) {
        presenterProperty.addListener((o, oldValue, newValue) -> presenterConsumer.accept(newValue));
    }

    /**
     * Gets the conventional name.
     *
     * @param ending the suffix to append
     * @return the conventional name with stripped ending
     */
    private String getConventionalName(@Nonnull final String ending) {
        return getConventionalName() + ending;
    }

    /**
     * Gets the conventional name.
     *
     * @return the name of the view without the "View" prefix in lowerCase. For
     * AirhacksView just airhacks is going to be returned.
     */
    private String getConventionalName() {
        return stripEnding(getClass().getSimpleName().toLowerCase());
    }

    /**
     * Gets the bundle name.
     *
     * @return the bundle name
     */
    private String getBundleName() {
        final String bundle;
        if (Strings.isNullOrEmpty(bundle = annotation.bundle())) {
            final String lbundle = getClass().getPackage().getName() + "." + getConventionalName();
            log.debug("Bundle: {} based on conventional name.", lbundle);
            return lbundle;
        }
        log.debug("Annotated bundle: {}", bundle);
        return bundle;
    }

    /**
     * Strip ending.
     *
     * @param clazz the clazz
     * @return the string
     */
    private static String stripEnding(@Nonnull final String clazz) {
        final String suffix = "view";
        if (!clazz.endsWith(suffix)) {
            return clazz;
        }
        return clazz.substring(0, clazz.lastIndexOf(suffix));
    }

    /**
     * Gets the fxml file path.
     *
     * @return the relative path to the fxml file derived from the FXML view.
     * e.g. The name for the AirhacksView is going to be /airhacks.fxml.
     */
    protected final String getFxmlPath() {
        final String fxmlPath = fxmlRoot + getConventionalName(".fxml");
        log.debug("Determined fxmlPath: " + fxmlPath);
        return fxmlPath;
    }

    /**
     * Returns a resource bundle if available
     *
     * @param name the name of the resource bundle.
     * @return the resource bundle
     */
    private ResourceBundle getResourceBundle(@Nonnull final String name) {
        try {
            log.debug("Resource bundle: " + name);
            return getBundle(name, new ResourceBundleControl(getResourceBundleCharset()));
        } catch (final MissingResourceException ex) {
            log.debug("No resource bundle could be determined: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Returns the charset to use when reading resource bundles as specified in
     * the annotation.
     *
     * @return the charset
     */
    private Charset getResourceBundleCharset() {
        return Charset.forName(annotation.encoding());
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
        return "AbstractFxmlView [presenterProperty=" + presenterProperty + ", bundle=" + bundle + ", resource=" + resource + ", fxmlRoot=" + fxmlRoot + "]";
    }
}
