package top.zenyoung.boot;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.boot.advice.ResponseFluxAdviceController;
import top.zenyoung.boot.config.AsyncConfig;
import top.zenyoung.boot.config.RepeatSubmitProperties;
import top.zenyoung.boot.config.SequenceProperties;
import top.zenyoung.boot.config.WebFluxConfig;

/**
 * WebFlux-自动注册
 *
 * @author young
 */
@Configuration
@ComponentScan({"top.zenyoung.boot.controller"})
@Import({AsyncConfig.class, WebFluxConfig.class, ResponseFluxAdviceController.class})
@EnableConfigurationProperties({RepeatSubmitProperties.class, SequenceProperties.class})
public class BootWebFluxAutoConfiguration {
    
}
