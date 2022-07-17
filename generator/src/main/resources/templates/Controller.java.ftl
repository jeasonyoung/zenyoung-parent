package ${basePackage}.${moduleName}.controller;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
<#if hasProvideServer=true>
import ${basePackage}.api.api.${moduleName}.${apiName}Api;
import ${basePackage}.api.dto.${moduleName}.${dtoName}DTO;
import ${basePackage}.api.vo.${moduleName}.${voName}VO;
<#else>
import ${basePackage}.common.dto.${moduleName}.${dtoName}DTO;
import ${basePackage}.common.vo.${moduleName}.${voName}VO;
</#if>
<#if hasOrm=true>
import ${basePackage}.common.model.${moduleName}.${poName};
</#if>
import ${basePackage}.${moduleName}.service.${serviceName}Service;
import import top.zenyoung.boot.controller.BaseController;
import top.zenyoung.common.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ${controllerName}Controller
 *
 * @author generator
 * date ${date?string("yyyy-MM-dd")}
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/${sourceName}s")
<#if hasProvideServer=true>
public class ${controllerName}Controller extends BaseController implements ${apiName}Api {
<#else>
public class ${controllerName}Controller extends BaseController {
</#if>
    private final ${serviceName}Service service;

    <#if hasProvideServer=true && hasBaseApi=true>
    @Override
    </#if>
    public ResultVO<${voName}VO> add(final ${dtoName}DTO dto){
        <#if hasOrm=true>
        ${poName} po = service.mapping(dto, ${poName}.class);
        po = service.add(po);
        return success(service.mapping(po, ${voName}VO.class));
        <#else>
        ///TODO:
        return success();
        </#if>
    }

    <#if hasProvideServer=true && hasBaseApi=true>
    @Override
    </#if>
    public ResultVO<${voName}VO> get(final ${idType} id){
        if(Objects.isNull(id)){
           return failed();
        }
        <#if hasOrm=true>
        final ${poName} po = service.getById(id);
        return success(service.mapping(po, ${voName}VO.class));
        <#else>
        ///TODO:
        return success();
        </#if>
    }

    <#if hasProvideServer=true && hasBaseApi=true>
    @Override
    </#if>
    public ResultVO<?> update(final ${idType} id, final ${dtoName}DTO dto){
        <#if hasOrm=true>
        final ${poName} po = service.mapping(dto, ${poName}.class);
        service.modify(id, po);
        return success();
        <#else>
        ///TODO:
        return success();
        </#if>
    }

    <#if hasProvideServer=true && hasBaseApi=true>
    @Override
    </#if>
    public ResultVO<?> delete(final ${idType}[] ids){
        if(Objects.isNull(ids)){
           return failed();
        }
        <#if hasOrm=true>
        service.delete(Lists.newArrayList(ids));
        <#else>
        ///TODO:
        </#if>
        return success();
    }
}