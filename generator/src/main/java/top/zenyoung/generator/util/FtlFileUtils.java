package top.zenyoung.generator.util;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.util.FtlUtils;
import top.zenyoung.generator.Constants;
import top.zenyoung.generator.db.Table;
import top.zenyoung.generator.dto.GeneratorDTO;
import top.zenyoung.generator.ftl.FtlFileGroup;
import top.zenyoung.generator.ftl.FtlFileInfo;
import top.zenyoung.generator.ftl.FtlFileType;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 模板文件工具类型
 *
 * @author young
 */
@Slf4j
public class FtlFileUtils {
    private static final FtlUtils FTL = FtlUtils.getInstance(FtlFileUtils.class);

    private static boolean checkIncludeGroup(@Nonnull final FtlFileType type, @Nonnull final GeneratorDTO dto) {
        final String includeGroup;
        if (!Strings.isNullOrEmpty(includeGroup = dto.getIncludeGroup())) {
            final String comma = ",", regex = "^[A-Z]+$";
            final List<String> groups = NameUtils.splitter(comma, includeGroup).stream()
                    .filter(g -> g.matches(regex))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(groups)) {
                for (String group : groups) {
                    try {
                        final FtlFileGroup g = Enum.valueOf(FtlFileGroup.class, group);
                        if (g.isEnable(type.getGroups())) {
                            return true;
                        }
                    } catch (Throwable e) {
                        log.warn("checkIncludeGroup: {} -exp: {}", group, e.getMessage());
                    }
                }
                return false;
            }
        }
        return true;
    }

    private static boolean checkIncludeFileTypes(@Nonnull final FtlFileType type, @Nonnull final GeneratorDTO dto) {
        final List<String> includeFileTypes = dto.getIncludeFileTypes();
        return CollectionUtils.isEmpty(includeFileTypes) || includeFileTypes.contains(type.name());
    }

    public static List<FtlFileInfo> build(@Nonnull final GeneratorDTO dto) {
        final Map<String, Object> metaMap = MetaMapUtils.getBasic(dto);
        final Boolean hasProvideServer = (Boolean) metaMap.get(Constants.PARAM_HAS_PROVIDE_SERVER);
        final Boolean baseBaseApi = (Boolean) metaMap.get(Constants.PARAM_HASH_BASE_API);
        final String baseDir = File.separator;
        return Stream.of(FtlFileType.values())
                .filter(f -> {
                    if (FtlFileGroup.Root.isEnable(f.getGroups())) {
                        return true;
                    }
                    if (!baseBaseApi && f == FtlFileType.BaseApi) {
                        return false;
                    }
                    return FtlFileGroup.HasSingle.isEnable(f.getGroups());
                })
                .filter(f -> checkIncludeGroup(f, dto))
                .filter(f -> checkIncludeFileTypes(f, dto))
                .map(f -> {
                    final FtlFileGroup group = hasProvideServer ? FtlFileGroup.Api : FtlFileGroup.Common;
                    FtlFileGroup g = FtlFileGroup.parse(f.getGroups(), group);
                    if (Objects.isNull(g)) {
                        g = FtlFileGroup.parse(f.getGroups(), FtlFileGroup.Common, FtlFileGroup.Service, FtlFileGroup.Root);
                    }
                    if (Objects.nonNull(g)) {
                        final String pkg = f.buildPackage(FTL, g, metaMap);
                        final String javaDir = f.checkFileSuffix(Constants.FILE_SUFFIX_JAVA) ? "src/main/java" : null;
                        final String dir = NameUtils.pathJoiner(baseDir, g.buildDirName(FTL, metaMap), javaDir, pkg);
                        final String fileName = f.buidFileName(FTL, metaMap);
                        return FtlFileInfo.of(FTL, f.getTemplate(), metaMap, dir, fileName);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static List<FtlFileInfo> build(@Nonnull final GeneratorDTO dto, @Nonnull final Table table) {
        final Map<String, Object> metaMap = MetaMapUtils.getTables(dto, table);
        final Boolean hasProvideServer = (Boolean) metaMap.get(Constants.PARAM_HAS_PROVIDE_SERVER);
        final Boolean hasMicro = (Boolean) metaMap.get(Constants.PARAM_HAS_MICRO);
        final Boolean hasOrm = (Boolean) metaMap.get(Constants.PARAM_HAS_ORM);
        final String baseDir = File.separator;
        return Stream.of(FtlFileType.values())
                .filter(f -> {
                    if (FtlFileGroup.Root.isEnable(f.getGroups())) {
                        return false;
                    }
                    if (!hasOrm && FtlFileGroup.HasOrm.isEnable(f.getGroups())) {
                        return false;
                    }
                    if (!hasMicro && FtlFileGroup.HasMicro.isEnable(f.getGroups())) {
                        return false;
                    }
                    return !FtlFileGroup.HasSingle.isEnable(f.getGroups());
                })
                .filter(f -> checkIncludeGroup(f, dto))
                .filter(f -> checkIncludeFileTypes(f, dto))
                .map(f -> {
                    String dir;
                    FtlFileGroup group = hasProvideServer ? FtlFileGroup.Api : FtlFileGroup.Common;
                    if (group.isEnable(f.getGroups())) {
                        final String pkg = f.buildPackage(FTL, group, metaMap);
                        dir = NameUtils.pathJoiner(baseDir, group.buildDirName(FTL, metaMap), "src/main/java", pkg);
                    } else {
                        group = FtlFileGroup.parse(f.getGroups(), FtlFileGroup.Common, FtlFileGroup.Service);
                        if (Objects.isNull(group)) {
                            return null;
                        }
                        final String pkg = f.buildPackage(FTL, group, metaMap);
                        dir = NameUtils.pathJoiner(baseDir, group.buildDirName(FTL, metaMap), "src/main/java", pkg);
                    }
                    final String fileName = f.buidFileName(FTL, metaMap);
                    return FtlFileInfo.of(FTL, f.getTemplate(), metaMap, dir, fileName);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
