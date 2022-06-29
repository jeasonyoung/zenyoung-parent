<#if isProvideServer=true>
package ${basePackage}.api.dto.${moduleName};
<#else>
package ${basePackage}.common.dto.${moduleName};
</#if>

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
<#list table.importPackages as pkg>
<#if !(pkg?ends_with("Date"))>
import ${pkg};
</#if>
</#list>

/**
 * ${table.comment}-DTO
 *
 * @author generator
 * date ${date?string("yyyy-MM-dd")}
 */
@Data
@ApiModel("${table.comment}-DTO")
public class ${dtoName}DTO implements Serializable {
<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>
    <#if field.isPrimaryKey || field.propertyType=="Date"
        || field.propertyName == "createUserCode"
        || field.propertyName == "updateUserCode">
        <#continue>
    </#if>
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
