package ${packageName}.vo;

/**
 * ${comment!}-响应报文
 * <#assign lastTime = .now>
 * @author ${author!}
 * @version 1.0
 * @date ${lastTime?string('yyyy-MM-dd HH:mm:ss')}
 **/
@Data
public class ${className}Res implements Serializable {
    <#list columns as col>
    /**
     * ${(col.comment)!}
     */
    <#if col.javaType == 'Date'>
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    </#if>
    private ${col.javaType} ${col.javaField};
    </#list>
}