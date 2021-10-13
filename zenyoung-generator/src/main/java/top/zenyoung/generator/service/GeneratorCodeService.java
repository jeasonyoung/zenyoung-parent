package top.zenyoung.generator.service;

import top.zenyoung.generator.domain.Column;
import top.zenyoung.generator.domain.Table;

import javax.annotation.Nonnull;
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
}
