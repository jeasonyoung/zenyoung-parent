package top.zenyoung.boot.config;

import com.github.xiaoymin.knife4j.core.extend.OpenApiExtendSetting;
import com.github.xiaoymin.knife4j.core.model.MarkdownProperty;
import com.github.xiaoymin.knife4j.spring.configuration.Knife4jProperties;
import com.github.xiaoymin.knife4j.spring.extension.OpenApiExtensionResolver;
import com.google.common.base.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.CollectionUtils;
import top.zenyoung.boot.annotation.ConditionalOnKnife4jEnable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Knife4j配置
 *
 * @author young
 */
@ConditionalOnKnife4jEnable
@ComponentScan(basePackages = {"com.github.xiaoymin.knife4j.spring.plugin"})
public class Knife4jConfig {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnKnife4jEnable
    public Knife4jProperties knife4jProperties(final SwaggerProperties properties) {
        final boolean knife4j = properties.isKnife4j();
        final Knife4jProperties knife4jProperties = new Knife4jProperties();
        knife4jProperties.setEnable(knife4j);
        final List<SwaggerProperties.SwaggerItemProperties> dockets = properties.getDockets();
        if (!CollectionUtils.isEmpty(dockets)) {
            final List<MarkdownProperty> markdownProperties = dockets.stream()
                    .filter(item -> !Strings.isNullOrEmpty(item.getDocName()))
                    .map(item -> {
                        final MarkdownProperty markdownProperty = new MarkdownProperty();
                        markdownProperty.setGroup(item.getGroup());
                        markdownProperty.setName(item.getDocName());
                        markdownProperty.setLocations(item.getDocPaths());
                        return markdownProperty;
                    })
                    .collect(Collectors.toList());
            knife4jProperties.setDocuments(markdownProperties);
        }
        return knife4jProperties;
    }

    @Bean(initMethod = "start")
    @ConditionalOnMissingBean
    @ConditionalOnKnife4jEnable
    public OpenApiExtensionResolver markdownResolver(final Knife4jProperties knife4jProperties) {
        OpenApiExtendSetting setting = knife4jProperties.getSetting();
        if (Objects.isNull(setting)) {
            setting = new OpenApiExtendSetting();
        }
        return new OpenApiExtensionResolver(setting, knife4jProperties.getDocuments());
    }
}
