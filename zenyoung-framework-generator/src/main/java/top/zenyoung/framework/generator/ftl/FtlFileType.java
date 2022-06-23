package top.zenyoung.framework.generator.ftl;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.util.FtlUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 模板文件类型-枚举
 *
 * @author young
 */
@Getter
public enum FtlFileType {
    ;

    private final EnumSet<FtlFileGroup> groups;
    private final String template;
    private final String fileName;
    private final EnumMap<FtlFileGroup, String> pkgMaps = new EnumMap<>(FtlFileGroup.class);

    FtlFileType(@Nonnull final EnumSet<FtlFileGroup> groups, @Nonnull final String template,
                @Nonnull final String fileName, @Nullable final Pkg... pkgs) {
        this.groups = groups;
        this.template = template;
        this.fileName = fileName;
        if (Objects.nonNull(pkgs)) {
            Stream.of(pkgs)
                    .filter(Objects::nonNull)
                    .forEach(p -> this.pkgMaps.put(p.group, p.pkg));
        }
    }

    /**
     * 构建包名
     *
     * @param ftl   FTL工具类
     * @param group 模板文件分组
     * @param args  参数集合
     * @return 包名
     */
    public String buildPackage(@Nonnull final FtlUtils ftl, @Nonnull final FtlFileGroup group, @Nonnull final Map<String, Object> args) {
        if (!CollectionUtils.isEmpty(this.pkgMaps)) {
            final String pkg = this.pkgMaps.get(group);
            if (!Strings.isNullOrEmpty(pkg)) {
                return ftl.dynamicProcess(pkg, args);
            }
        }
        return null;
    }

    /**
     * 构建文件名
     *
     * @param ftl  FTL工具类
     * @param args 参数集合
     * @return 文件名
     */
    public String buidFileName(@Nonnull final FtlUtils ftl, @Nonnull final Map<String, Object> args) {
        if (!Strings.isNullOrEmpty(this.fileName)) {
            return ftl.dynamicProcess(this.fileName, args);
        }
        return null;
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class Pkg {
        private final FtlFileGroup group;
        private final String pkg;
    }
}
