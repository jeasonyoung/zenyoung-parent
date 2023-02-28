package top.zenyoung.boot.config;

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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;
import top.zenyoung.boot.util.EnvUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * App 配置
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor
@Import({SwaggerProperties.class})
public class SwaggerConfig implements InitializingBean, BeanPostProcessor {
    private final ApplicationContext context;

    @Value("${spring.application.name:default}")
    private String appName;

    @Value("${spring.profiles.active:default}")
    private String bootProfile;

    @Value("${spring.cloud.config.profile:default}")
    private String cloudProfile;

    @Override
    public void afterPropertiesSet() throws Exception {
        final boolean isDevOrTest = EnvUtils.isDevOrTest(bootProfile, cloudProfile);
        final SwaggerBeanCreator beanCreator = SwaggerBeanCreator.of(context, appName, isDevOrTest);
        beanCreator.create();
    }

    @Override
    public Object postProcessBeforeInitialization(@Nonnull final Object bean, @Nonnull final String beanName) throws BeansException {
        return bean;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Object postProcessAfterInitialization(@Nonnull final Object bean, @Nonnull final String beanName) throws BeansException {
        //处理swagger在SpringBoot2.6以上的不可用问题
        final DefaultListableBeanFactory defaultListableBeanFactory = context.getBean(DefaultListableBeanFactory.class);
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

    @RequiredArgsConstructor(staticName = "of")
    private static class SwaggerBeanCreator {
        private final ApplicationContext context;
        private final String appName;
        private final boolean isDevOrTest;

        private SwaggerProperties properties;
        private boolean knife4jEnable;

        public void create() {
            final DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
            this.properties = context.getBean(SwaggerProperties.class);
            this.knife4jEnable = this.properties.getKnife4j();
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
            if(Strings.isNullOrEmpty(groupName)){
                groupName = "default";
            }
            final Docket docket = new Docket(DocumentationType.SWAGGER_2)
                    .apiInfo(apiInfo())
                    .groupName(groupName)
                    .enable(isDevOrTest);
            if(knife4jEnable){
                final OpenApiExtensionResolver resolver = context.getBean(OpenApiExtensionResolver.class);
                docket.extensions(resolver.buildExtensions(groupName));
            }
            final ApiSelectorBuilder builder = docket.select();
            final String apis = item.getApis();
            if(Strings.isNullOrEmpty(apis)){
                builder.apis(RequestHandlerSelectors.any());
            }else{
                builder.apis(RequestHandlerSelectors.basePackage(apis));
            }
            final String paths = item.getPaths();
            if(Strings.isNullOrEmpty(paths)){
                builder.paths(PathSelectors.any());
            }else{
                builder.paths(PathSelectors.regex(paths));
            }
            builder.build();
            return docket;
        }

        @SuppressWarnings({"unchecked"})
        private static <T> T getFieldValue(@Nullable final OpenApiExtensionResolver resolver, @Nonnull final String name) {
            if (Objects.nonNull(resolver) && !Strings.isNullOrEmpty(name)) {
                final Field field = ReflectionUtils.findField(resolver.getClass(), name);
                if (Objects.nonNull(field)) {
                    try {
                        return (T) field.get(resolver);
                    } catch (Throwable e) {
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
                final Map<String, List<OpenApiExtendMarkdownFile>> markdownFileMap = getFieldValue(resolver, "markdownFileMaps");
                docket.extensions(buildExtensions(markdownFileMap, knife4jProperties));
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
            if (CollectionUtils.isEmpty(annMap)) {
                final List<String> suffixs = Lists.newArrayList("App", "Application");
                for (Map.Entry<String, Object> entry : annMap.entrySet()) {
                    final String key = entry.getKey();
                    final Class<?> cls = entry.getValue().getClass();
                    if (!Strings.isNullOrEmpty(key) && Objects.nonNull(cls)) {
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
            final List<OpenApiExtendMarkdownFile> all = markdownFileMaps.values().stream()
                    .map(Lists::newArrayList)
                    .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
            final OpenApiExtension openApiExtension = new OpenApiExtension("x-openapi");
            openApiExtension.addProperty(new OpenApiSettingExtension(knife4jProperties.getSetting()));
            openApiExtension.addProperty(new OpenApiMarkdownExtension(all));
            return Lists.newArrayList(openApiExtension);
        }
    }
}
