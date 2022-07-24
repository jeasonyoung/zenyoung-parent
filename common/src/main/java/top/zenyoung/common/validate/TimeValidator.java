package top.zenyoung.common.validate;

import com.google.common.base.Strings;
import top.zenyoung.common.util.TextValidUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 手机号校验类
 *
 * @author young
 */
public class TimeValidator implements ConstraintValidator<TimeValid, String> {
    private boolean checkEmpty;

    @Override
    public void initialize(final TimeValid valid) {
        checkEmpty = valid.checkEmpty();
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (Strings.isNullOrEmpty(value)) {
            return !checkEmpty;
        }
        return TextValidUtils.isTime(value);
    }
}
