package top.zenyoung.framework;

import com.google.common.base.Joiner;

import javax.annotation.Nonnull;

/**
 * 常量
 *
 * @author young
 */
public class Constants {
    /**
     * redis分隔符
     */
    public static final String SEP_REDIS = ":";
    /**
     * 常量前缀
     */
    public static final String PREFIX = "zy-framework" + SEP_REDIS;

    /**
     * 拼接串
     *
     * @param joins 拼接集合
     * @return 拼接后字符串
     */
    public static String join(@Nonnull final String... joins) {
        return Joiner.on(SEP_REDIS).join(joins);
    }
}
