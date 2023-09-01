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
}
