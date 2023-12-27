package top.zenyoung.common.valid;

import com.google.common.base.Strings;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import top.zenyoung.common.util.TextValidUtils;


/**
 * 日期校验
 *
 * @author young
 */
public class DateValidator implements ConstraintValidator<DateValid, String> {
    private boolean checkEmpty;

    @Override
    public void initialize(final DateValid valid) {
        this.checkEmpty = valid.checkEmpty();
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (Strings.isNullOrEmpty(value)) {
            return !this.checkEmpty;
        }
        return TextValidUtils.isDate(value);
    }
}
