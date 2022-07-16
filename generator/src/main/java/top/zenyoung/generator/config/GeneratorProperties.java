package top.zenyoung.generator.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 代码生成配置
 *
 * @author young
 */
@Data
@ApiModel("代码生成配置")
public class GeneratorProperties implements Serializable {
    /**
     * 服务名称
     */
    @ApiModelProperty("服务名称")
    private String serverName;
    /**
     * 模块名称
     */
    @ApiModelProperty("模块名称")
    private String moduleName;
    /**
     * 模块名称忽略字符(多个用,分隔)
     */
    @ApiModelProperty("模块名称忽略字符(多个用,分隔)")
    private String moduleNameIgnores = "-";
    /**
     * 基础包名
     */
    @ApiModelProperty("基础包名")
    private String basePackageName;
    /**
     * 数据库名
     */
    @ApiModelProperty("数据库名")
    private String dbName;
    /**
     * 包含表名集合
     */
    @ApiModelProperty("表名集合")
    private List<String> includeTableNames;
    /**
     * 表名规则表达式
     */
    @ApiModelProperty("表名规则表达式")
    private String tableNameRuleRegex;
    /**
     * id类型
     */
    @ApiModelProperty("id类型")
    private String idType = Long.class.getSimpleName();
    /**
     * 是否提供服务
     */
    @ApiModelProperty("是否提供服务")
    private Boolean hasProvideService = true;
    /**
     * 是否含有BaseApi
     */
    @ApiModelProperty("是否含有BaseApi")
    private Boolean hasBaseApi = true;
    /**
     * 是否包含 orm 默认true
     */
    @ApiModelProperty("是否包含ORM")
    private Boolean hasOrm = true;
    /**
     * 是否微服务,默认true
     */
    @ApiModelProperty("是否微服务")
    private Boolean hasMicro = false;
}
