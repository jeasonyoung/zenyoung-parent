package top.zenyoung.graphics;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.zenyoung.graphics.config.CaptchaProperties;
import top.zenyoung.graphics.config.GraphicsProperties;
import top.zenyoung.graphics.service.CaptchaService;
import top.zenyoung.graphics.service.CaptchaStorageService;
import top.zenyoung.graphics.service.impl.CaptchaServiceImpl;
import top.zenyoung.graphics.service.impl.CaptchaStorageMemoryServiceImpl;

/**
 * 图形图像-注入
 *
 * @author young
 */
@Configuration
@EnableConfigurationProperties({GraphicsProperties.class, CaptchaProperties.class})
public class GraphicsAutoConfig {
    /**
     * 构建验证码存储-服务接口
     *
     * @return 验证码存储-服务
     */
    @Bean
    @ConditionalOnMissingBean
    public CaptchaStorageService buildStorageService() {
        return CaptchaStorageMemoryServiceImpl.of();
    }

    /**
     * 构建验证码-服务接口
     *
     * @param opCtx   Spring上下文
     * @param opProps 配置属性
     * @return 验证码-服务
     */
    @Bean
    @ConditionalOnMissingBean
    public CaptchaService buildCaptchaService(final ObjectProvider<ApplicationContext> opCtx,
                                              final ObjectProvider<CaptchaProperties> opProps) {
        return CaptchaServiceImpl.of(opCtx.getIfAvailable(), opProps.getIfAvailable()).init();
    }
}
