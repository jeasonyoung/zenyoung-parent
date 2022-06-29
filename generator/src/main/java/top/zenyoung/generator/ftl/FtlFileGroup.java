package top.zenyoung.generator.ftl;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.util.FtlUtils;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 模板文件分组
 *
 * @author young
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum FtlFileGroup {
    /**
     * 根模块
     */
    Root("${serverName}"),
    /**
     * API模块
     */
    Api("${serverName}-api"),
    /**
     * 公共模块
     */
    Common("${serverName}-common"),
    /**
     * 业务模块
     */
    Service("${serverName}-service"),

    /**
     * 是否单列模板
     */
    HasSingle(null),
    /**
     * 是否ORM
     */
    HasOrm(null),
    /**
     * 是否Micro
     */
    HasMicro(null);

    /**
     * 分组目录
     */
    private final String dir;

    /**
     * 检查模板文件分组是否启用
     *
     * @param groupSets 模板文件分组集合
     * @return 是否启用
     */
    public boolean isEnable(@Nonnull final EnumSet<FtlFileGroup> groupSets) {
        return Objects.nonNull(parse(groupSets, this));
    }

    /**
     * 获取目录名称
     *
     * @param ftl  ftl工具
     * @param args 参数集合
     * @return 目录名称
     */
    public String buildDirName(@Nonnull final FtlUtils ftl, @Nonnull final Map<String, Object> args) {
        if (!CollectionUtils.isEmpty(args) && !Strings.isNullOrEmpty(this.dir)) {
            return ftl.dynamicProcess(this.dir, args);
        }
        return null;
    }

    /**
     * 模板文件分组解析
     *
     * @param groupSets 模板文件分组集合
     * @param group     检查模板文佳分组
     * @return 模板文件分组
     */
    public static FtlFileGroup parse(@Nonnull final EnumSet<FtlFileGroup> groupSets, @Nonnull final FtlFileGroup... group) {
        if (!CollectionUtils.isEmpty(groupSets) && group.length > 0) {
            return groupSets.stream()
                    .filter(gs -> Stream.of(group).anyMatch(g -> gs == g))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
