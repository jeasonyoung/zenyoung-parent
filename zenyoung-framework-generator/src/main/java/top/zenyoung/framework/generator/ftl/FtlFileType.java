package top.zenyoung.framework.generator.ftl;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.util.FtlUtils;
import top.zenyoung.framework.generator.Constants;

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
    /**
     * BaseAPI
     */
    BaseApi(EnumSet.of(FtlFileGroup.Api, FtlFileGroup.HasSingle),
            "BaseApi.java.ftl", "BaseApi" + Constants.FILE_SUFFIX_JAVA,
            Pkg.of(FtlFileGroup.Api, "${basePackage}.api.base")),
    /**
     * API
     */
    Api(EnumSet.of(FtlFileGroup.Api), "Api.java.ftl",
            String.format("${%1$s}Api%2$s", Constants.PARAM_API_NAME, Constants.FILE_SUFFIX_JAVA),
            Pkg.of(FtlFileGroup.Api, "${basePackage}.api.api.${moduleName}")),
    /**
     * DTO
     */
    DTO(EnumSet.of(FtlFileGroup.Api, FtlFileGroup.Common), "DTO.java.ftl",
            String.format("${%1$s}DTO%2$s", Constants.PARAM_DTO_NAME, Constants.FILE_SUFFIX_JAVA),
            Pkg.of(FtlFileGroup.Api, "${basePackage}.api.dto.${moduleName}"),
            Pkg.of(FtlFileGroup.Common, "${basePackage}.common.dto.${moduleName}")),
    /**
     * VO
     */
    VO(EnumSet.of(FtlFileGroup.Api, FtlFileGroup.Common), "VO.java.ftl",
            String.format("${%1$s}VO%2$s", Constants.PARAM_VO_NAME, Constants.FILE_SUFFIX_JAVA),
            Pkg.of(FtlFileGroup.Api, "${basePackage}.api.vo.${moduleName}"),
            Pkg.of(FtlFileGroup.Common, "${basePackage}.common.vo.${moduleName}")),
    /**
     * PO-Model
     */
    PO(EnumSet.of(FtlFileGroup.Common, FtlFileGroup.HasOrm), "Model.java.ftl",
            String.format("${%1$s}%2$s", Constants.PARAM_PO_NAME, Constants.FILE_SUFFIX_JAVA),
            Pkg.of(FtlFileGroup.Common, "${basePackage}.common.model.${moduleName}")),
    /**
     * Mapper
     */
    Mapper(EnumSet.of(FtlFileGroup.Service), "Mapper.java.ftl",
            String.format("${%1$s}Mapper%2$s", Constants.PARAM_MAPPER_NAME, Constants.FILE_SUFFIX_JAVA),
            Pkg.of(FtlFileGroup.Service, "${basePackage}.${moduleName}.mapper")),
    /**
     * Mapper Xml
     */
    MapperXml(EnumSet.of(FtlFileGroup.Service), "Mapper.xml.ftl",
            String.format("${%1$s}Mapper%2$s", Constants.PARAM_MAPPER_XML_NAME, Constants.FILE_SUFFIX_XML),
            Pkg.of(FtlFileGroup.Service, "${basePackage}.${moduleName}.mapper")),
    /**
     * 控制器
     */
    Controller(EnumSet.of(FtlFileGroup.Service), "Controller.java.ftl",
            String.format("${%1$s}Controller%2$s", Constants.PARAM_CONTROLLER_NAME, Constants.FILE_SUFFIX_JAVA),
            Pkg.of(FtlFileGroup.Service, "${basePackage}.${moduleName}.controller")),
    /**
     * 服务接口
     */
    Service(EnumSet.of(FtlFileGroup.Service), "Service.java.ftl",
            String.format("${%1$s}Service%2$s", Constants.PARAM_SERVICE_NAME, Constants.FILE_SUFFIX_JAVA),
            Pkg.of(FtlFileGroup.Service, "${basePackage}.${moduleName}.service")),
    /**
     * 服务接口实现
     */
    ServiceImpl(EnumSet.of(FtlFileGroup.Service), "ServiceImpl.java.ftl",
            String.format("${%1$s}ServiceImpl%2$s", Constants.PARAM_SERVICE_IMPL_NAME, Constants.FILE_SUFFIX_JAVA),
            Pkg.of(FtlFileGroup.Service, "${basePackage}.${moduleName}.service.impl")),
    /**
     * AppMain
     */
    AppMain(EnumSet.of(FtlFileGroup.Service, FtlFileGroup.HasSingle), "AppMain.java.ftl",
            "AppMain" + Constants.FILE_SUFFIX_JAVA,
            Pkg.of(FtlFileGroup.Service, "${basePackage}")),
    /**
     * GIT忽略文件
     */
    GitIgnore(EnumSet.of(FtlFileGroup.Root, FtlFileGroup.HasSingle), ".gitignore.ftl", ".gitignore"),

    /**
     * 项目POM
     */
    PomRoot(EnumSet.of(FtlFileGroup.Root, FtlFileGroup.HasSingle), "PomRoot.xml.ftl", Constants.TYPE_POM),
    /**
     * API模块POM
     */
    PomApi(EnumSet.of(FtlFileGroup.Api, FtlFileGroup.HasSingle), "PomApi.xml.ftl", Constants.TYPE_POM),
    /**
     * 公共模块POM
     */
    PomCommon(EnumSet.of(FtlFileGroup.Common, FtlFileGroup.HasSingle), "PomCommon.xml.ftl", Constants.TYPE_POM),
    /**
     * 业务模块POM
     */
    PomService(EnumSet.of(FtlFileGroup.Service, FtlFileGroup.HasSingle), "PomService.xml.ftl", Constants.TYPE_POM),

    /**
     * Logback日志配置文件
     */
    LogbackSpring(EnumSet.of(FtlFileGroup.Service, FtlFileGroup.HasSingle), "Logback-spring.xml.ftl",
            "logback-spring" + Constants.FILE_SUFFIX_XML,
            Pkg.of(FtlFileGroup.Service, "src/main/resources")),
    /**
     * Bootstrap配置yaml
     */
    Bootstrap(EnumSet.of(FtlFileGroup.Service, FtlFileGroup.HasSingle), "bootstrap.yaml.ftl",
            "bootstrap.yaml",
            Pkg.of(FtlFileGroup.Service, "src/main/resources")),
    /**
     * Entrypoint Shell
     */
    Entrypoint(EnumSet.of(FtlFileGroup.Service, FtlFileGroup.HasSingle), "entrypoint.sh.ftl", "entrypoint.sh"),
    /**
     * Dockerfile
     */
    Dockerfile(EnumSet.of(FtlFileGroup.Service, FtlFileGroup.HasSingle), "Dockerfile.ftl", "Dockerfile");

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

    /**
     * 检查文件后缀
     *
     * @param suffix 文件后缀
     * @return 检查结果
     */
    public boolean checkFileSuffix(@Nonnull final String suffix) {
        if (!Strings.isNullOrEmpty(this.fileName) && !Strings.isNullOrEmpty(suffix)) {
            return this.fileName.endsWith(suffix);
        }
        return false;
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class Pkg {
        private final FtlFileGroup group;
        private final String pkg;
    }
}
