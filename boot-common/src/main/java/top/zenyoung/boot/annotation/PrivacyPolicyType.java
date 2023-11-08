package top.zenyoung.boot.annotation;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

/**
 * 隐私保费类型枚举
 *
 * @author young
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum PrivacyPolicyType {
    /**
     * 手机号码
     */
    MOBILE("^(\\d{3})\\d+(\\d{4})$", "$1****$2"),
    /**
     * 身份证号码
     */
    IDNUMBER("^(\\d{6})\\d+([0-9|X]{4})$", "$1****$2");

    private final String regex;
    private final String replacement;

    /**
     * 获取脱敏数据
     *
     * @param val 源数据
     * @return 脱敏数据
     */
    public String getPrivacy(@Nullable final String val) {
        if (!Strings.isNullOrEmpty(val)) {
            return val.replaceAll(regex, replacement);
        }
        return val;
    }
}
