<#if hasProvideServer=true>
package ${basePackage}.api.vo.${moduleName};
<#else>
package ${basePackage}.common.vo.${moduleName};
</#if>

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
<#list table.importPackages as pkg>
import ${pkg};
</#list>

/**
 * ${table.comment}-VO
 *
 * @author generator
 * ${date?string("yyyy-MM-dd")}
 */
@Data
@ApiModel("${table.comment}-VO")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ${voName}VO implements Serializable {
<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>
    <#if field.comment!?length gt 0>
    /**
     * ${field.comment}
     */
    @ApiModelProperty("${field.comment}")
    </#if>
    private ${field.propertyType} ${field.propertyName};

</#list>
<#------------  END 字段循环遍历  ---------->
}
