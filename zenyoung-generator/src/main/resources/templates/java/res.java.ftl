package ${packageName}.vo;

/**
 * ${comment!}-响应报文
 * <#assign lastTime = .now>
 * @author ${author!}
 * @version 1.0
 * @date ${lastTime?iso_utc}
 **/
@Data
public class ${className}Res implements Serializable {
    <#list columns as col>
    /**
     * ${(col.comment)!}
     */
    private ${col.javaType} ${col.javaField};
    </#list>
}