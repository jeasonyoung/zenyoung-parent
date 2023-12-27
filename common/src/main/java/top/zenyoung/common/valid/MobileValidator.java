package top.zenyoung.common.valid;

import com.google.common.base.Strings;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import top.zenyoung.common.util.TextValidUtils;


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
