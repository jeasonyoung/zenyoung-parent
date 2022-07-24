package top.zenyoung.common.validate;

import com.google.common.base.Strings;
import top.zenyoung.common.util.TextValidUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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
