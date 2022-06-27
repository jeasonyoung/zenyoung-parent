package ${basePackage}.${moduleName}.service;

<#if hasOrm=true>
import top.zenyoung.framework.service.BaseOrmService;
import ${basePackage}.common.model.${moduleName}.${poName};
<#else>
import top.zenyoung.framework.service.BaseService;
</#if>

/**
 * ${serviceName}-服务接口
 *
 * @author generator
 * date ${date?string("yyyy-MM-dd")}
 */
<#if hasOrm=true>
public interface ${serviceName}Service extends BaseOrmService<${poName}, ${idType}>{
<#else>
public interface ${serviceName}Service extends BaseService {
</#if>

}