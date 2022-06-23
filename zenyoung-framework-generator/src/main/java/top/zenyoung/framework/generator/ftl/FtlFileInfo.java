package top.zenyoung.framework.generator.ftl;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.zenyoung.common.util.FtlUtils;
import top.zenyoung.framework.generator.util.NameUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * 模板文件信息
 *
 * @author young
 */
@Getter
@RequiredArgsConstructor(staticName = "of")
public class FtlFileInfo {
    private static final FtlUtils FTL = FtlUtils.getInstance(FtlFileInfo.class);

    /**
     * 模板文件
     */
    private final FtlFileType fileType;
    /**
     * 模板数据参数
     */
    private final Map<String, Object> args;
    /**
     * 文件路径
     */
    private String path;
    /**
     * 文件名
     */
    private String name;
    /**
     * 文件分组
     */
    private FtlFileGroup group;

    /**
     * 初始化模板
     *
     * @param root  根目录
     * @param dir   资源目录
     * @param group 模板分组
     */
    public void initTemplate(@Nonnull final String root, @Nullable final String dir, @Nonnull final FtlFileGroup group) {
        this.group = group;
        final String pkg = this.fileType.buildPackage(FTL, group, this.args);
        final String groupDir = group.getDirName(FTL, args);
        this.path = NameUtils.pathJoiner(root, groupDir, dir, pkg);
        //文件名
        this.name = this.fileType.buidFileName(FTL, args);
    }

    /**
     * 初始化模板
     *
     * @param root  根目录
     * @param group 模板分组
     */
    public void initTemplate(@Nonnull final String root, @Nonnull final FtlFileGroup group) {
        this.initTemplate(root, null, group);
    }

    /**
     * 构建模板文件内容
     *
     * @return 模板文件内容
     */
    public String buildFtlContent() {
        final String ftlName = this.fileType.getTemplate();
        if (!Strings.isNullOrEmpty(ftlName)) {
            return FTL.process(ftlName, this.args);
        }
        return null;
    }
}
