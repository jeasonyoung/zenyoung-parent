package top.zenyoung.graphics;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.zenyoung.graphics.config.GraphicsProperties;
import top.zenyoung.graphics.service.CaptchaService;
import top.zenyoung.graphics.service.CaptchaStorageService;
import top.zenyoung.graphics.service.impl.CaptchaServiceImpl;
import top.zenyoung.graphics.service.impl.CaptchaStorageMemoryServiceImpl;

import javax.annotation.Nonnull;

/**
 * 图形图像-注入
 *
 * @author young
 */
@Configuration
@EnableConfigurationProperties({GraphicsProperties.class})
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
     * @param context Spring上下文
     * @param props   配置属性
     * @return 验证码-服务
     */
    @Bean
    @ConditionalOnMissingBean
    public CaptchaService buildCaptchaService(@Nonnull final ApplicationContext context,
                                              @Nonnull final GraphicsProperties props) {
        final GraphicsProperties.Captcha captcha = props.getCaptcha();
        return CaptchaServiceImpl.of(context, captcha).init();
    }
}
