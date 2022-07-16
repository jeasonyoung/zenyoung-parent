package ${basePackage}.${moduleName}.controller;

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
import java.util.Arrays;
</#if>
import ${basePackage}.${moduleName}.service.${serviceName}Service;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.common.vo.ResultVO;
import io.swagger.annotations.ApiOperation;
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
    @ApiOperation(value="创建", notes="根据${dtoName}DTO对象创建")
    public ResultVO<${voName}VO> add(@RequestBody final ${dtoName}DTO dto){
        <#if hasOrm=true>
        ${poName} po = service.mapping(dto, ${poName}.class);
        po = service.add(po);
        final ${voName}VO vo = service.mapping(po, ${voName}VO.class);
        return success(vo);
        <#else>
        ///TODO:
        return success();
        </#if>
    }

    <#if hasProvideServer=true && hasBaseApi=true>
    @Override
    </#if>
    @ApiOperation(value="获取详细信息", notes="根据url的id来获取详细信息")
    public ResultVO<${voName}VO> get(@PathVariable final ${idType} id){
        if(StringUtils.isBlank(id)){
           return failed();
        }
        <#if hasOrm=true>
        final ${poName} po = service.getById(id);
        final ${voName}VO vo = service.mapping(po, ${voName}VO.class);
        return success(vo);
        <#else>
        ///TODO:
        return success();
        </#if>
    }

    <#if hasProvideServer=true && hasBaseApi=true>
    @Override
    </#if>
    @ApiOperation(value="更新详细信息", notes="根据url的id来指定更新对象，并根据传过来的${dtoName}DTO信息来更新详细信息")
    public ResultVO<Void> update(@PathVariable final ${idType} id, @RequestBody final ${dtoName}DTO dto){
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
    @ApiOperation(value="删除", notes="根据url的id来指定删除对象")
    public ResultVO<Void> delete(@PathVariable final ${idType}[] ids){
        if(Objects.isNull(id)){
           return failed();
        }
        <#if hasOrm=true>
        service.delete(ids);
        <#else>
        ///TODO:
        </#if>
        return success();
    }
}