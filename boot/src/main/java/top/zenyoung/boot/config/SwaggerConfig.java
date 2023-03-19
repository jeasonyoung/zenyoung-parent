package top.zenyoung.boot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import top.zenyoung.boot.registrar.SwaggerBeanCreator;
import top.zenyoung.boot.util.EnvUtils;

/**
 * App 配置
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor
@Import({SwaggerProperties.class})
public class SwaggerConfig implements InitializingBean {
    private final ApplicationContext context;

    @Value("${spring.application.name:default}")
    private String appName;

    @Value("${spring.profiles.active:default}")
    private String bootProfile;

    @Value("${spring.cloud.config.profile:default}")
    private String cloudProfile;

    @Override
    public void afterPropertiesSet() {
        final boolean isDevOrTest = EnvUtils.isDevOrTest(bootProfile, cloudProfile);
        final SwaggerBeanCreator beanCreator = SwaggerBeanCreator.of(context, appName, isDevOrTest);
        beanCreator.create();
    }

//    @Override
//    public Object postProcessBeforeInitialization(@Nonnull final Object bean, @Nonnull final String beanName) throws BeansException {
//        return bean;
//    }
//
//    @Override
//    @SuppressWarnings({"unchecked"})
//    public Object postProcessAfterInitialization(@Nonnull final Object bean, @Nonnull final String beanName) throws BeansException {
//        //处理swagger在SpringBoot2.6以上的不可用问题
//        final DefaultListableBeanFactory defaultListableBeanFactory = context.getBean(DefaultListableBeanFactory.class);
//        final boolean modify = (bean instanceof WebMvcRequestHandlerProvider) && defaultListableBeanFactory.containsBean(beanName);
//        if (modify) {
//            try {
//                final Field handlerMappingsField = ReflectionUtils.findField(WebMvcRequestHandlerProvider.class, "handlerMappings");
//                if (Objects.nonNull(handlerMappingsField)) {
//                    handlerMappingsField.setAccessible(true);
//                    final List<RequestMappingInfoHandlerMapping> handlerMappings = (List<RequestMappingInfoHandlerMapping>) ReflectionUtils.getField(handlerMappingsField, bean);
//                    if (Objects.nonNull(handlerMappings)) {
//                        final List<RequestMappingInfoHandlerMapping> items = handlerMappings.stream().filter(m -> Objects.isNull(m.getPatternParser())).collect(Collectors.toList());
//                        handlerMappings.clear();
//                        if (!CollectionUtils.isEmpty(items)) {
//                            handlerMappings.addAll(items);
//                        }
//                    }
//                }
//            } catch (Throwable e) {
//                log.warn("修改WebMvcRequestHandlerProvider的属性:handlerMappings出错,可能导致Swagger不可用=> {}", e.getMessage());
//            }
//        }
//        return bean;
//    }
}
