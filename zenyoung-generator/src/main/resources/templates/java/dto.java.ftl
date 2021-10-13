package ${packageName}.dao.dto;

<#list javaImports as imp>
import ${imp};
</#list>

import lombok.Data;
import java.io.Serializable;

/**
 * ${comment}-数据DTO
 * <#assign lastTime = .now>
 * @author ${author}
 * @version 1.0
 * @date ${lastTime?iso_utc}
 **/
@Data
public class ${className}DTO implements Serializable {
    <#list columns as col>
    /**
     * ${col.comment}
     */
    private ${col.javaType} ${col.javaField};
    </#list>
}