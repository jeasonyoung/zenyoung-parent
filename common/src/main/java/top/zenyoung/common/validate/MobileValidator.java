package top.zenyoung.common.validate;

import com.google.common.base.Strings;
import top.zenyoung.common.util.TextValidUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 手机号校验
 *
 * @author young
 */
public class MobileValidator implements ConstraintValidator<Mobile, String> {

    @Override
    public void initialize(final Mobile mobile) {

    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (Strings.isNullOrEmpty(value)) {
            return false;
        }
        return TextValidUtils.isMobile(value);
    }
}
