package top.zenyoung.generator.ftl;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.zenyoung.common.util.FtlUtils;

import java.util.Map;

/**
 * 模板文件信息
 *
 * @author young
 */
@RequiredArgsConstructor(staticName = "of")
public class FtlFileInfo {
    /**
     * 模板工具类
     */
    private final FtlUtils ftl;
    /**
     * 模板文件
     */
    private final String ftlName;
    /**
     * 模板参数
     */
    private final Map<String, Object> args;
    /**
     * 文件路径
     */
    @Getter
    private final String fileDir;
    /**
     * 文件名
     */
    @Getter
    private final String fileName;

    /**
     * 构建模板文件内容
     *
     * @return 模板文件内容
     */
    public String buildFtlContent() {
        final String name;
        if (!Strings.isNullOrEmpty(name = this.ftlName)) {
            return ftl.process(name, this.args);
        }
        return null;
    }
}
