package top.zenyoung.code.generator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * WebFlux 配置
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/1/18 6:00 下午
 **/
@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {

}
