package ${packageName}.dao.entity;

<#list javaImports as imp>
import ${imp};
</#list>
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
<#if logicDelete>
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
</#if>
import javax.persistence.*;

/**
 * ${comment}-数据实体
 * <#assign lastTime = .now>
 * @author ${author}
 * @version 1.0
 * @date ${lastTime?iso_utc}
 **/
@Data
@Entity
@Table(name="${tableName}")
<#if logicDelete>
@SQLDelete(sql = "update ${tableName} set status = -1 where id = ?")
@Where(clause = "status >= 0")
</#if>
public class ${className}Entity {
    <#list columns as col>
    /**
     * ${col.comment}
     */
    <#if col.primaryKey>
    @Id
    <#if !col.increment && col.javaType == 'Long' && col.javaField == 'id'>
    @GeneratedValue(generator = "snowFlake")
    @GenericGenerator(name = "snowFlake", strategy = "top.zenyoung.data.generator.SnowFlakeIdentityGenerator")
    </#if>
    <#elseif col.timestamp>
    @Temporal(TemporalType.TIMESTAMP)
    </#if>
    @Column(name = "${col.columnName}"<#if col.required>,nullable = false</#if>)
    private ${col.javaType} ${col.javaField};
    </#list>
}