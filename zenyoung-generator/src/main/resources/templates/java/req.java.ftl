package ${packageName}.vo;

/**
 * ${comment!}-请求报文
 * <#assign lastTime = .now>
 * @author ${author!}
 * @version 1.0
 * @date ${lastTime?iso_utc}
 **/
@Data
public class ${className}Req implements Serializable {
    <#list columns as col>
    /**
     * ${(col.comment)!}
     */
    private ${col.javaType} ${col.javaField};
    </#list>
}