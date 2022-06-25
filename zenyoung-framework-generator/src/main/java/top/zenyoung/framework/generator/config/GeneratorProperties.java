package top.zenyoung.framework.generator.config;

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
     * 服务名称
     */
    @ApiModelProperty("服务名称")
    private String serverName;
}
