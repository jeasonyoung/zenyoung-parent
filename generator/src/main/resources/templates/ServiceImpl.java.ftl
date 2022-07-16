package ${basePackage}.${moduleName}.service.impl;

import lombok.RequiredArgsConstructor;
<#if hasOrm=true>
import top.zenyoung.orm.mapper.BaseMapper;
import ${basePackage}.common.model.${moduleName}.${poName};
import ${basePackage}.${moduleName}.mapper.${mapperName}Mapper;
import top.zenyoung.orm.service.impl.BaseOrmServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
<#else>
import top.zenyoung.boot.service.impl.BaseServiceImpl;
</#if>
import ${basePackage}.${moduleName}.service.${serviceName}Service;
import org.springframework.stereotype.Service;;

/**
 * ${serviceName}-服务实现
 *
 * @author generator
 * date ${date?string("yyyy-MM-dd")}
 */
@Service
@RequiredArgsConstructor
<#if hasOrm=true>
public class ${serviceName}ServiceImpl extends BaseOrmServiceImpl<${poName}, ${idType}> implements ${serviceName}Service {
    private final ${mapperName}Mapper ${mapperNameFirstLower}Mapper;

    @Nonnull
    @Override
    protected BaseMapper<${poName}, ${idType}> getMapper() {
        return this.${mapperNameFirstLower}Mapper;
    }
<#else>
public class ${serviceName}ServiceImpl extends BaseServiceImpl implements ${serviceName}Service {
</#if>

}
