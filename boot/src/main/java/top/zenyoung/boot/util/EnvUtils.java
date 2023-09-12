package top.zenyoung.boot.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * 环境工具类
 *
 * @author young
 */
@UtilityClass
public class EnvUtils {
    private static final List<String> DEV_TEST_PROFILES = Lists.newArrayList("local", "dev", "test");

    /**
     * 获取是否是测试或开发环境
     *
     * @param profiles profiles
     * @return 是否是测试或开发环境
     */
    public static boolean isDevOrTest(@Nullable final String... profiles) {
        if (Objects.nonNull(profiles)) {
            for (final String profile : profiles) {
                if (!Strings.isNullOrEmpty(profile) && DEV_TEST_PROFILES.contains(profile.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
}
