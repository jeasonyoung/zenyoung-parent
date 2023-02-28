package top.zenyoung.boot.config;

import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;
import java.util.List;

/**
 * Swagger配置
 *
 * @author young
 */
@Data
@Configuration
@ConfigurationProperties("top.zenyoung.swagger")
public class SwaggerProperties implements Serializable {
    /**
     * 是否启用knife4j
     */
    private Boolean knife4j = Boolean.TRUE;
    /**
     * 标题
     */
    private String title = "";
    /**
     * 版本号
     */
    private String version = "1.0.0";
    /**
     * 服务地址
     */
    private String termsOfServiceUrl = "http://localhost";
    /**
     * 联系信息
     */
    private ContactProperties contact = new ContactProperties();
    /**
     * Dockets
     */
    private List<SwaggerItemProperties> dockets = Lists.newArrayList();

    /**
     * 联系信息
     */
    @Data
    public static class ContactProperties implements Serializable {
        /**
         * 名称
         */
        private String name = "";
        /**
         * 链接地址
         */
        private String url = "";
        /**
         * 邮箱地址
         */
        private String email = "";
    }

    /**
     * Swagger项配置
     */
    @Data
    public static class SwaggerItemProperties implements Serializable {
        /**
         * 分组名称
         */
        private String group;
        /**
         * Api命名空间
         */
        private String apis;
        /**
         * 路径地址
         */
        private String paths;
        /**
         * 文档名称
         */
        private String docName;
        /**
         * 文档路径
         */
        private String docPaths;
    }
}
