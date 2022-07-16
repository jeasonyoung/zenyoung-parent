package ${basePackage}.${moduleName}.mapper;

import ${basePackage}.common.model.${moduleName}.${poName};
import top.zenyoung.orm.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ${apiName}-Mapper
 *
 * @author generator
 * date ${date?string("yyyy-MM-dd")}
 */
@Mapper
public interface ${mapperName}Mapper extends BaseMapper<${poName}, ${idType}> {

}