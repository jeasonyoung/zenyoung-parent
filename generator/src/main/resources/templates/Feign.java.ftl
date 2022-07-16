package ${basePackage}.api.feign.${moduleName};

import ${basePackage}.api.api.${moduleName}.${apiName}Api;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * ${controllerName}Feign
 *
 * @author generator
 * date ${date?string("yyyy-MM-dd")}
 */
@FeignClient(value = "zy-${serverName}-server", path = "/${sourceName}s")
public interface ${apiName}Feign extends ${apiName}Api {

}
