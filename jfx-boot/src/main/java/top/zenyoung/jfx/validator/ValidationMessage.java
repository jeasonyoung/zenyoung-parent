package top.zenyoung.jfx.validator;

import lombok.Getter;

import javax.annotation.Nullable;

/**
 * A validation message represents the description of a single problem.
 *
 * @author r.lichtenberger@synedra.com
 */
@Getter
public class ValidationMessage {
    private final String text;
    private final Severity severity;

    public ValidationMessage(@Nullable final Severity severity, @Nullable final String text) {
        this.severity = severity;
        this.text = text;
    }
}
