package top.zenyoung.jfx.validator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import lombok.EqualsAndHashCode;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A check represents a check for validity in a form.
 *
 * @author r.lichtenberger@synedra.com
 */
@EqualsAndHashCode
public class Check implements Serializable {
    private final Map<String, ObservableValue<?>> dependencies = Maps.newHashMap();
    private final ReadOnlyObjectWrapper<ValidationResult> validationResultProperty = new ReadOnlyObjectWrapper<>();
    private final List<Node> targets = Lists.newArrayList();
    private final List<Decoration> decorations = Lists.newArrayList();

    private Consumer<Context> checkMethod;
    private ValidationResult nextValidationResult = new ValidationResult();
    private Function<ValidationMessage, Decoration> decorationFactory;
    private final ChangeListener<?> dependencyListener;

    public class Context {

        private Context() {
        }

        /**
         * Get the current value of a dependency.
         *
         * @param <T> The type the value should be casted into
         * @param key The key the dependency has been given
         * @return The current value of the given depency
         */
        @SuppressWarnings("unchecked")
        public <T> T get(@Nonnull final String key) {
            return (T) dependencies.get(key).getValue();
        }

        public Iterable<String> keys() {
            return dependencies.keySet();
        }

        /**
         * Emit a warning.
         *
         * @param message The text to be presented to the user as warning message.
         */
        public void warn(@Nonnull final String message) {
            nextValidationResult.addWarning(message);
        }

        /**
         * Emit an error.
         *
         * @param message The text to be presented to the user as error message.
         */
        public void error(@Nonnull final String message) {
            nextValidationResult.addError(message);
        }
    }

    public Check() {
        validationResultProperty.set(new ValidationResult());
        decorationFactory = DefaultDecoration.getFactory();
        dependencyListener = (obs, oldv, newv) -> recheck();
    }

    public Check withMethod(@Nonnull final Consumer<Context> checkMethod) {
        this.checkMethod = checkMethod;
        return this;
    }

    public Check dependsOn(@Nonnull final String key, @Nonnull final ObservableValue<?> dependency) {
        dependencies.put(key, dependency);
        return this;
    }

    public Check decorates(@Nonnull final Node target) {
        targets.add(target);
        return this;
    }

    public Check decoratingWith(@Nonnull final Function<ValidationMessage, Decoration> decorationFactory) {
        this.decorationFactory = decorationFactory;
        return this;
    }

    /**
     * Sets this check to be immediately evaluated if one of its dependencies changes.
     * This method must be called last.
     */
    @SuppressWarnings({"all"})
    public Check immediate() {
        for (final ObservableValue<?> dependency : dependencies.values()) {
            dependency.addListener((ChangeListener) dependencyListener);
        }
        Platform.runLater(this::recheck);    // to circumvent problems with decoration pane vs. dialog
        return this;
    }

    /**
     * Evaluate all dependencies and apply decorations of this check. You should not normally need to call this method directly.
     */
    public void recheck() {
        nextValidationResult = new ValidationResult();
        checkMethod.accept(new Context());
        for (final Node target : targets) {
            for (final Decoration decoration : decorations) {
                decoration.remove(target);
            }
        }
        decorations.clear();
        for (final Node target : targets) {
            for (final ValidationMessage validationMessage : nextValidationResult.getMessages()) {
                final Decoration decoration = decorationFactory.apply(validationMessage);
                decorations.add(decoration);
                decoration.add(target);
            }
        }
        if (!nextValidationResult.getMessages().equals(getValidationResult().getMessages())) {
            validationResultProperty.set(nextValidationResult);
        }
    }

    /**
     * Retrieves current validation result
     *
     * @return validation result
     */
    public ValidationResult getValidationResult() {
        return validationResultProperty.get();
    }

    /**
     * Can be used to track validation result changes
     *
     * @return The Validation result property.
     */
    public ReadOnlyObjectProperty<ValidationResult> validationResultProperty() {
        return validationResultProperty.getReadOnlyProperty();
    }
}
