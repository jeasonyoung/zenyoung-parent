package top.zenyoung.generator;

import io.swagger.annotations.ApiOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import top.zenyoung.generator.config.GenConfig;

/**
 * 代码生成器_spring-boot-start
 *
 * @author young
 */
@Configuration
@ConditionalOnClass(GenConfig.class)
@ConditionalOnProperty(prefix = "top.zenyoung.generator", value = "enable", matchIfMissing = true)
@EnableConfigurationProperties(GenConfig.class)
public class GeneratorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Docket createDocketApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("代码生成器接口文档")
                .description("适用用于代码生成器前端接口文档")
                .version("1.0")
                .build();
    }
}
