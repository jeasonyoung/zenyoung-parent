package top.zenyoung.jfx.validator;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * A Validator collects several checks and sums up their ValidationResults.
 *
 * @author r.lichtenberger@synedra.com
 */
public class Validator {
    private final Map<Check, ChangeListener<ValidationResult>> checks = Maps.newLinkedHashMap();
    private final ReadOnlyObjectWrapper<ValidationResult> validationResultProperty = new ReadOnlyObjectWrapper<>(new ValidationResult());
    private final ReadOnlyBooleanWrapper containsWarningsProperty = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper containsErrorsProperty = new ReadOnlyBooleanWrapper();

    /**
     * Create a check that lives within this checker's domain.
     *
     * @return A check object whose dependsOn, decorates, etc. methods can be called
     */
    public Check createCheck() {
        final Check check = new Check();
        this.add(check);
        return check;
    }

    /**
     * Add another check to the checker. Changes in the check's validationResultProperty will be reflected in the checker.
     *
     * @param check The check to add.
     */
    public void add(@Nonnull final Check check) {
        final ChangeListener<ValidationResult> listener = (obs, oldv, newv) -> refreshProperties();
        checks.put(check, listener);
        check.validationResultProperty().addListener(listener);
    }

    /**
     * Removes a check from this validator.
     *
     * @param check The check to remove from this validator.
     */
    public void remove(@Nonnull final Check check) {
        final ChangeListener<ValidationResult> listener = checks.remove(check);
        if (listener != null) {
            check.validationResultProperty().removeListener(listener);
        }
        refreshProperties();
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

    /**
     * A read-only boolean property indicating whether any of the checks of this validator emitted a warning.
     */
    public ReadOnlyBooleanProperty containsWarningsProperty() {
        return containsWarningsProperty.getReadOnlyProperty();
    }

    public boolean containsWarnings() {
        return containsWarningsProperty().get();
    }

    /**
     * A read-only boolean property indicating whether any of the checks of this validator emitted an error.
     */
    public ReadOnlyBooleanProperty containsErrorsProperty() {
        return containsErrorsProperty.getReadOnlyProperty();
    }

    public boolean containsErrors() {
        return containsErrorsProperty().get();
    }

    /**
     * Run all checks (decorating nodes if appropriate)
     *
     * @return true if no errors were found, false otherwise
     */
    public boolean validate() {
        for (final Check check : checks.keySet()) {
            check.recheck();
        }
        return !containsErrors();
    }

    private void refreshProperties() {
        final ValidationResult nextResult = new ValidationResult();
        for (final Check check : checks.keySet()) {
            nextResult.addAll(check.getValidationResult().getMessages());
        }
        validationResultProperty.set(nextResult);
        boolean hasWarnings = false;
        boolean hasErrors = false;
        for (final ValidationMessage msg : nextResult.getMessages()) {
            hasWarnings = hasWarnings || msg.getSeverity() == Severity.WARNING;
            hasErrors = hasErrors || msg.getSeverity() == Severity.ERROR;
        }
        containsWarningsProperty.set(hasWarnings);
        containsErrorsProperty.set(hasErrors);
    }

    /**
     * Create a string property that depends on the validation result.
     * Each error message will be displayed on a separate line prefixed with a bullet.
     *
     * @return StringBinding
     */
    public StringBinding createStringBinding() {
        return createStringBinding("• ", "\n", Severity.ERROR);
    }

    /**
     * Create a string property that depends on the validation result.
     *
     * @param prefix     The string to prefix each validation message with
     * @param separator  The string to separate consecutive validation messages with
     * @param severities The severities to consider; If none is given, only Severity.ERROR will be considered
     * @return StringBinding
     */
    public StringBinding createStringBinding(@Nonnull final String prefix, @Nonnull final String separator,
                                             @Nonnull final Severity... severities) {
        final Set<Severity> wanted = Sets.newHashSet(Arrays.asList(severities));
        if (wanted.isEmpty()) {
            wanted.add(Severity.ERROR);
        }
        return Bindings.createStringBinding(() -> {
            final StringBuilder str = new StringBuilder();
            for (final ValidationMessage msg : validationResultProperty.get().getMessages()) {
                if (wanted.contains(msg.getSeverity())) {
                    if (str.length() > 0) {
                        str.append(separator);
                    }
                    str.append(prefix).append(msg.getText());
                }
            }
            return str.toString();
        }, validationResultProperty);
    }
}
