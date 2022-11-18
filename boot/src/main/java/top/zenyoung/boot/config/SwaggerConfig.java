package top.zenyoung.boot.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * App 配置
 *
 * @author young
 */
@Slf4j
@Configuration
@EnableKnife4j
@EnableSwagger2WebMvc
public class SwaggerConfig implements BeanPostProcessor {
    @Autowired
    private DefaultListableBeanFactory defaultListableBeanFactory;

    @Bean
    @ConditionalOnMissingBean
    public Docket createDocketApi() {
        return new Docket(DocumentationType.SWAGGER_2)
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
                .version("3.0")
                .build();
    }

    @Override
    public Object postProcessBeforeInitialization(@Nonnull final Object bean, @Nonnull final String beanName) throws BeansException {
        return bean;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Object postProcessAfterInitialization(@Nonnull final Object bean, @Nonnull final String beanName) throws BeansException {
        //处理swagger在SpringBoot2.6以上的不可用问题
        final boolean modify = (bean instanceof WebMvcRequestHandlerProvider) && defaultListableBeanFactory.containsBean(beanName);
        if (modify) {
            try {
                final Field handlerMappingsField = ReflectionUtils.findField(WebMvcRequestHandlerProvider.class, "handlerMappings");
                if (Objects.nonNull(handlerMappingsField)) {
                    handlerMappingsField.setAccessible(true);
                    final List<RequestMappingInfoHandlerMapping> handlerMappings = (List<RequestMappingInfoHandlerMapping>) ReflectionUtils.getField(handlerMappingsField, bean);
                    if (Objects.nonNull(handlerMappings)) {
                        final List<RequestMappingInfoHandlerMapping> items = handlerMappings.stream().filter(m -> Objects.isNull(m.getPatternParser())).collect(Collectors.toList());
                        handlerMappings.clear();
                        if (!CollectionUtils.isEmpty(items)) {
                            handlerMappings.addAll(items);
                        }
                    }
                }
            } catch (Throwable e) {
                log.warn("修改WebMvcRequestHandlerProvider的属性:handlerMappings出错,可能导致Swagger不可用=> {}", e.getMessage());
            }
        }
        return bean;
    }
}
