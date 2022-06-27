package ${basePackage}.common.model.${moduleName};

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.zenyoung.framework.model.BasePO;
<#list table.importPackages as pkg>
import ${pkg};
</#list>

/**
 * ${table.comment}-数据模型
 *
 * @author generator
 * date ${date?string("yyyy-MM-dd")}
 */
@Data
@TableName("${table.name}")
public class ${poName} implements BasePO<${idType}> {
<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>
    <#if field.isPrimaryKey>
        <#assign keyPropertyName="${field.propertyName}"/>
    </#if>
    <#if field.comment!?length gt 0>
    /**
     * ${field.comment}
     */
    </#if>
    <#if field.isPrimaryKey>
    <#-- 主键 -->
    @TableId("${field.name}")
    </#if>
    private ${field.propertyType} ${field.propertyName};
</#list>
<#------------  END 字段循环遍历  ---------->
}
