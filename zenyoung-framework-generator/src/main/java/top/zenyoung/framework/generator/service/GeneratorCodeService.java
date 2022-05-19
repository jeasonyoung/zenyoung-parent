package top.zenyoung.framework.generator.service;

import top.zenyoung.framework.generator.domain.Column;
import top.zenyoung.framework.generator.domain.Table;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 代码生成-服务接口
 *
 * @author young
 */
public interface GeneratorCodeService {

    /**
     * 代码生成
     *
     * @param table   表数据
     * @param columns 表字段集合
     * @return 生成代码文件名及代码内容集合
     */
    Map<String, String> generatorCodes(@Nonnull final Table table, @Nonnull final List<Column> columns);

    /**
     * 构建Zip压缩包
     *
     * @param fileMaps 文件内容Map
     * @param output   Zip压缩输出流
     */
    void buildZipStream(@Nullable final Map<String, String> fileMaps, @Nonnull final OutputStream output);
}
