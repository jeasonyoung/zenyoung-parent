<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${basePackage}.${moduleName}.mapper.${mapperName}Mapper">
    <!-- 通用查询映射结果 -->
    <resultMap id="BaseResultMap" type="${basePackage}.common.model.${moduleName}.${poName}">
    <#list table.fields as field>
    <#if field.isPrimaryKey><#--生成主键排在第一位-->
        <id column="${field.name}" property="${field.propertyName}" />
    </#if>
    </#list>
    <#list table.fields as field>
    <#if !field.isPrimaryKey><#--生成普通字段 -->
        <result column="${field.name}" property="${field.propertyName}" />
    </#if>
    </#list>
    </resultMap>
</mapper>
