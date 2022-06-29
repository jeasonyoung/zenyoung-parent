package ${basePackage};

<#if hasMicro=true>
import top.zenyoung.boot.annotation.Micro;
<#else>
import top.zenyoung.boot.annotation.Boot;
</#if>

import org.springframework.boot.SpringApplication;

/**
 * 入口函数
 *
 * @author generator
 * date ${date?string("yyyy-MM-dd")}
 */
<#if hasMicro=true>
@Micro
<#else>
@Boot
</#if>
public class AppMain {
    /**
     * 主函数
     * @param args 参数
     */
    public static void main(final String[] args) {
        SpringApplication.run(AppMain.class, args);
    }
}