package top.zenyoung.boot.registrar;

import com.github.xiaoymin.knife4j.core.extend.OpenApiExtendMarkdownFile;
import com.github.xiaoymin.knife4j.spring.configuration.Knife4jProperties;
import com.github.xiaoymin.knife4j.spring.extension.OpenApiExtension;
import com.github.xiaoymin.knife4j.spring.extension.OpenApiExtensionResolver;
import com.github.xiaoymin.knife4j.spring.extension.OpenApiMarkdownExtension;
import com.github.xiaoymin.knife4j.spring.extension.OpenApiSettingExtension;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import top.zenyoung.boot.config.SwaggerProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Swagger Bean 创建
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class SwaggerBeanCreator {
    private final ApplicationContext context;
    private final String appName;
    private final boolean isDevOrTest;

    private SwaggerProperties properties;
    private boolean knife4jEnable;

    public void create() {
        final DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
        this.properties = context.getBean(SwaggerProperties.class);
        this.knife4jEnable = this.properties.isKnife4j();
        final String simpleName = Docket.class.getSimpleName();
        final List<SwaggerProperties.SwaggerItemProperties> dockets = properties.getDockets();
        if (!CollectionUtils.isEmpty(dockets)) {
            for (int i = 0; i < dockets.size(); i++) {
                registerSyntheticBeanIfMissing(beanFactory, simpleName + "-" + i, dockets.get(i));
            }
            return;
        }
        registerSyntheticBeanIfMissing(beanFactory, simpleName, null);
    }

    private void registerSyntheticBeanIfMissing(@Nonnull final DefaultListableBeanFactory beanFactory,
                                                @Nonnull final String name,
                                                @Nullable final SwaggerProperties.SwaggerItemProperties item) {
        final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
                Docket.class,
                () -> createDocket(item)
        );
        beanFactory.registerBeanDefinition(name, builder.getRawBeanDefinition());
    }

    private Docket createDocket(@Nullable final SwaggerProperties.SwaggerItemProperties item) {
        if (Objects.isNull(item)) {
            return defaultDocket();
        }
        String groupName = item.getGroup();
        if (Strings.isNullOrEmpty(groupName)) {
            groupName = "default";
        }
        final Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName(groupName)
                .enable(isDevOrTest);
        if (knife4jEnable) {
            final OpenApiExtensionResolver resolver = context.getBean(OpenApiExtensionResolver.class);
            docket.extensions(resolver.buildExtensions(groupName));
        }
        final ApiSelectorBuilder builder = docket.select();
        final String apis = item.getApis();
        if (Strings.isNullOrEmpty(apis)) {
            builder.apis(RequestHandlerSelectors.any());
        } else {
            builder.apis(RequestHandlerSelectors.basePackage(apis));
        }
        final String paths = item.getPaths();
        if (Strings.isNullOrEmpty(paths)) {
            builder.paths(PathSelectors.any());
        } else {
            builder.paths(PathSelectors.regex(paths));
        }
        builder.build();
        return docket;
    }

    @SuppressWarnings({"unchecked"})
    private static <T> T getFieldValue(@Nullable final OpenApiExtensionResolver resolver) {
        if (Objects.nonNull(resolver)) {
            final String name = "markdownFileMaps";
            final Field field = ReflectionUtils.findField(resolver.getClass(), name);
            if (Objects.nonNull(field)) {
                try {
                    field.setAccessible(true);
                    return (T) field.get(resolver);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    log.error("getFieldValue(resolver: {},name: {})[field: {}]-exp: {}", resolver, name, field, e.getMessage());
                }
            }
        }
        return null;
    }

    private Docket defaultDocket() {
        final Docket docket = new Docket(DocumentationType.SWAGGER_2);
        if (knife4jEnable) {
            final OpenApiExtensionResolver resolver = context.getBean(OpenApiExtensionResolver.class);
            final Knife4jProperties knife4jProperties = context.getBean(Knife4jProperties.class);
            //
            final Map<String, List<OpenApiExtendMarkdownFile>> markdownFileMaps = getFieldValue(resolver);
            if (Objects.nonNull(markdownFileMaps)) {
                docket.extensions(buildExtensions(markdownFileMaps, knife4jProperties));
            }
        }
        final String apiPath = defaultApiPath();
        docket.apiInfo(apiInfo())
                .groupName(appName)
                .enable(isDevOrTest)
                .select()
                .apis(RequestHandlerSelectors.basePackage(apiPath))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    private String defaultApiPath() {
        String path = "top.zenyoung";
        final Map<String, Object> annMap = context.getBeansWithAnnotation(ComponentScan.class);
        if (!CollectionUtils.isEmpty(annMap)) {
            final List<String> suffixs = Stream.of("App", "Application", "AppMain")
                    .map(String::toLowerCase)
                    .filter(s-> !Strings.isNullOrEmpty(s))
                    .collect(Collectors.toList());
            for (Map.Entry<String, Object> entry : annMap.entrySet()) {
                final String key = entry.getKey().toLowerCase();
                final Class<?> cls = entry.getValue().getClass();
                if (!Strings.isNullOrEmpty(key)) {
                    for (String suffix : suffixs) {
                        if (key.endsWith(suffix)) {
                            return cls.getPackage().getName();
                        }
                    }
                }
            }
        }
        return path;
    }

    private ApiInfo apiInfo() {
        final SwaggerProperties.ContactProperties p = properties.getContact();
        Contact contact;
        if (Objects.isNull(p)) {
            contact = new Contact("zenyoung", "", "");
        } else {
            contact = new Contact(p.getName(), p.getUrl(), p.getEmail());
        }
        return new ApiInfoBuilder()
                .title(properties.getTitle())
                .termsOfServiceUrl(properties.getTermsOfServiceUrl())
                .contact(contact)
                .version(properties.getVersion())
                .build();
    }

    @SuppressWarnings({"all"})
    private List<VendorExtension> buildExtensions(
            @Nullable final Map<String, List<OpenApiExtendMarkdownFile>> markdownFileMaps,
            @Nonnull final Knife4jProperties knife4jProperties) {
        if (CollectionUtils.isEmpty(markdownFileMaps)) {
            return Lists.newArrayList();
        }
        final List<OpenApiExtendMarkdownFile> all = markdownFileMaps.values()
                .stream()
                .map(Lists::newArrayList)
                .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
        final OpenApiExtension openApiExtension = new OpenApiExtension("x-openapi");
        openApiExtension.addProperty(new OpenApiSettingExtension(knife4jProperties.getSetting()));
        openApiExtension.addProperty(new OpenApiMarkdownExtension(all));
        return Lists.newArrayList(openApiExtension);
    }
}
