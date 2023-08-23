package top.zenyoung.common.util;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Pattern;

/**
 * 文本验证工具类
 *
 * @author young
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TextValidUtils {
    private static final Pattern REGEX_MOBILE = Pattern.compile("^1\\d{10}$");
    private static final Pattern REGEX_TEL = Pattern.compile("^(\\d{3,4}-)?\\d{6,8}$");
    private static final Pattern REGEX_ID_CARD15 = Pattern.compile("^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0-2]\\d)|3[0-1])\\d{3}$");
    private static final Pattern REGEX_ID_CARD18 = Pattern.compile("^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0-2]\\d)|3[0-1])\\d{3}(\\d|[X|x])$");
    private static final Pattern REGEX_EMAIL = Pattern.compile("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
    private static final Pattern REGEX_URL = Pattern.compile("[a-zA-Z]+://[^\\s]*");
    /**
     * 正则：yyyy-MM-dd格式的日期校验，已考虑平闰年
     */
    private static final Pattern REGEX_DATE = Pattern.compile("^(?:(?!0000)[\\d]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[\\d]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:\\d{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)$");
    /**
     * 正则：yyyy-MM-dd HH:mm:ss格式的时间校验，已考虑平闰年
     */
    private static final Pattern REGEX_TIME = Pattern.compile("^((?:(?!0000)\\d{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:\\d{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)\\s([0-1]\\d|2[0-3]):([0-5]\\d):([0-5]\\d))$");
    private static final Pattern REGEX_IP = Pattern.compile("((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)");

    public static boolean isMatch(@Nonnull final Pattern pattern, @Nullable final CharSequence input) {
        return !Strings.isNullOrEmpty((String) input) && pattern.matcher(input).matches();
    }

    /**
     * 验证手机号码是否符合格式
     *
     * @param input 待验证手机号码
     * @return 是否符合格式
     */
    public static boolean isMobile(@Nullable final CharSequence input) {
        return isMatch(REGEX_MOBILE, input);
    }

    /**
     * 验证固定电话号码
     *
     * @param input 待验证固定电话号码
     * @return 是否符合格式
     */
    public static boolean isTel(@Nullable final CharSequence input) {
        return isMatch(REGEX_TEL, input);
    }

    /**
     * 验证身份证号码
     *
     * @param input 待验证身份证号码
     * @return 是否符合格式
     */
    public static boolean isIdCard(@Nullable final CharSequence input) {
        return isMatch(REGEX_ID_CARD15, input) || isMatch(REGEX_ID_CARD18, input);
    }

    /**
     * 验证邮箱
     *
     * @param input 待验证邮箱
     * @return 是否符合格式
     */
    public static boolean isEmail(@Nullable final CharSequence input) {
        return isMatch(REGEX_EMAIL, input);
    }

    /**
     * 验证URL
     *
     * @param input 待验证URL
     * @return 是否符合格式
     */
    public static boolean isUrl(@Nullable final CharSequence input) {
        return isMatch(REGEX_URL, input);
    }

    /**
     * 验证日期(yyyy-MM-dd),已考虑闰年
     *
     * @param input 待验证日期
     * @return 是否符合格式
     */
    public static boolean isDate(@Nullable final CharSequence input) {
        return isMatch(REGEX_DATE, input);
    }

    /**
     * 验证时间(yyyy-MM-dd HH:mm:ss)
     *
     * @param input 待验证时间,已考虑平闰年
     * @return 是否符合格式
     */
    public static boolean isTime(@Nullable final CharSequence input) {
        return isMatch(REGEX_TIME, input);
    }

    /**
     * 验证IP地址
     *
     * @param input 待验证IP
     * @return 是否符合格式
     */
    public static boolean isIp(@Nullable final CharSequence input) {
        return isMatch(REGEX_IP, input);
    }
}
