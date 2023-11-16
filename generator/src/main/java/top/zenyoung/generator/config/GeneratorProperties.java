package top.zenyoung.generator.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 代码生成配置
 *
 * @author young
 */
@Data
@Schema(description = "代码生成配置")
public class GeneratorProperties implements Serializable {
    /**
     * 服务名称
     */
    @Schema(description = "服务名称")
    private String serverName;
    /**
     * 模块名称
     */
    @Schema(description = "模块名称")
    private String moduleName;
    /**
     * 模块名称忽略字符(多个用,分隔)
     */
    @Schema(description = "模块名称忽略字符(多个用,分隔)")
    private String moduleNameIgnores = "-";
    /**
     * 基础包名
     */
    @Schema(description = "基础包名")
    private String basePackageName;
    /**
     * 数据库名
     */
    @Schema(description = "数据库名")
    private String dbName;
    /**
     * 包含表名集合
     */
    @Schema(description = "表名集合")
    private List<String> includeTableNames;
    /**
     * 表名规则表达式
     */
    @Schema(description = "表名规则表达式")
    private String tableNameRuleRegex;
    /**
     * id类型
     */
    @Schema(description = "id类型")
    private String idType = Long.class.getSimpleName();
    /**
     * 是否提供服务
     */
    @Schema(description = "是否提供服务")
    private Boolean hasProvideService = true;
    /**
     * 是否含有BaseApi
     */
    @Schema(description = "是否含有BaseApi")
    private Boolean hasBaseApi = true;
    /**
     * 是否包含 orm 默认true
     */
    @Schema(description = "是否包含ORM")
    private Boolean hasOrm = true;
    /**
     * 是否微服务,默认true
     */
    @Schema(description = "是否微服务")
    private Boolean hasMicro = false;
}
