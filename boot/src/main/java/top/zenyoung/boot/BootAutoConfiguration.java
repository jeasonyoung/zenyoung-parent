package top.zenyoung.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.request.WebRequest;
import top.zenyoung.boot.advice.ExceptionController;
import top.zenyoung.boot.config.*;
import top.zenyoung.boot.service.BeanMappingService;
import top.zenyoung.boot.service.CaptchaService;
import top.zenyoung.boot.service.impl.BeanMappingServiceImpl;
import top.zenyoung.boot.service.impl.CaptchaServiceImpl;
import top.zenyoung.boot.util.IdSequenceUtils;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.common.util.JsonUtils;
import top.zenyoung.common.util.MapUtils;
import top.zenyoung.common.vo.ResultVO;

import java.util.Map;
import java.util.Objects;

/**
 * Boot-自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@ComponentScan({
        "top.zenyoung.boot.aop",
        "top.zenyoung.boot.resolver",
        "top.zenyoung.boot.interceptor",
        "top.zenyoung.boot.controller"
})
@Import({AsyncConfig.class, WebConfig.class, SwaggerConfig.class, ExceptionController.class})
@EnableConfigurationProperties({RepeatSubmitProperties.class, CaptchaProperties.class, IdSequenceProperties.class})
public class BootAutoConfiguration {
    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public ErrorAttributes buildErrorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public Map<String, Object> getErrorAttributes(final WebRequest webRequest, final ErrorAttributeOptions options) {
                final Map<String, Object> retMap = super.getErrorAttributes(webRequest, options);
                final ResultVO<?> vo = ResultVO.ofFail(JsonUtils.toJson(objectMapper, retMap));
                try {
                    final Integer status = MapUtils.getVal(retMap, "status", Integer.class);
                    if (Objects.nonNull(status)) {
                        vo.setCode(status);
                    }
                } catch (Throwable e) {
                    log.warn("getErrorAttributes(webRequest: {},options: {})-exp: {}", webRequest, options, e.getMessage());
                }
                return MapUtils.from(vo);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public IdSequence buildSequence(final ObjectProvider<IdSequenceProperties> provider) {
        return IdSequenceUtils.create(provider.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public BeanMappingService beanMappingService() {
        return new BeanMappingServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "top.zenyoung.captcha", name = "enable", havingValue = "true")
    public CaptchaService captchaService(final ObjectProvider<CaptchaProperties> properties,
                                         final ObjectProvider<ApplicationContext> contexts) {
        final CaptchaProperties cp = properties.getIfAvailable();
        final ApplicationContext ctx = contexts.getIfAvailable();
        final CaptchaServiceImpl impl = new CaptchaServiceImpl(cp, ctx);
        //初始化
        impl.init();
        //返回
        return impl;
    }
}
