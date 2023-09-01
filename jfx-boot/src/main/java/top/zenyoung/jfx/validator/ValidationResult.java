package top.zenyoung.jfx.validator;

import com.google.common.collect.Lists;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A validation result consists of 0 ... n validation messages.
 *
 * @author r.lichtenberger@synedra.com
 */
@Getter
public class ValidationResult {
    private final List<ValidationMessage> messages = Lists.newArrayList();

    public void addWarning(@Nonnull final String text) {
        messages.add(new ValidationMessage(Severity.WARNING, text));
    }

    public void addError(@Nonnull final String text) {
        messages.add(new ValidationMessage(Severity.ERROR, text));
    }

    public void addAll(@Nonnull final List<ValidationMessage> messages) {
        this.messages.addAll(messages);
    }
}
