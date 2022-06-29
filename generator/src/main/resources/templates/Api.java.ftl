package ${basePackage}.api.api.${moduleName};

<#if hasBaseApi=true>
import ${basePackage}.api.base.BaseApi;
import ${basePackage}.api.dto.${moduleName}.${dtoName}DTO;
import ${basePackage}.api.vo.${moduleName}.${voName}VO;
</#if>

/**
 * ${apiName}API接口
 *
 * @author generator
 * date ${date?string("yyyy-MM-dd")}
 */
<#if hasBaseApi=true>
public interface ${apiName}Api extends BaseApi<${dtoName}DTO, ${voName}VO> {
<#else>
public interface ${apiName}Api {
</#if>

}