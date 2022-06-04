package top.zenyoung.framework.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.common.sequence.SnowFlake;
import top.zenyoung.framework.auth.AuthProperties;
import top.zenyoung.framework.auth.BaseAuthenticationManagerService;
import top.zenyoung.framework.runtime.config.RuntimeProperties;
import top.zenyoung.framework.runtime.service.impl.*;
import top.zenyoung.framework.service.AuthCaptchaService;
import top.zenyoung.framework.service.RedisEnhancedService;
import top.zenyoung.security.token.TokenLimitService;
import top.zenyoung.security.token.TokenService;
import top.zenyoung.security.token.TokenVerifyService;
import top.zenyoung.service.BeanMappingService;
import top.zenyoung.service.QueueService;
import top.zenyoung.service.SyncLockService;

/**
 * 运行时模块-自动配置
 *
 * @author young
 */
@Configuration
@ConditionalOnClass(RuntimeProperties.class)
@EnableConfigurationProperties(RuntimeProperties.class)
@ComponentScan(basePackages = {
        "top.zenyoung.framework.runtime.advice",
        "top.zenyoung.framework.runtime.aspectj",
        "top.zenyoung.framework.runtime.config",
        "top.zenyoung.framework.runtime.controller"
})
@ConditionalOnProperty(prefix = "top.zenyoung.runtime", value = "enable", matchIfMissing = true)
public class RuntimeAutoConfiguration {
    @Autowired
    private ObjectMapper objMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RuntimeProperties properties;

    @Autowired
    private ApplicationContext context;

    @Bean
    @ConditionalOnMissingBean(IdSequence.class)
    public IdSequence buildSequence() {
        final int max = 10;
        final int cpus = Math.max(Runtime.getRuntime().availableProcessors(), 1);
        return SnowFlake.getInstance(cpus & max, (cpus * 2) & max);
    }

    @Bean
    @ConditionalOnMissingBean(AuthProperties.class)
    public AuthProperties getAuthConfig() {
        return properties.getAuth();
    }

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean(BeanMappingService.class)
    public BeanMappingService mappingService() {
        return new BeanMappingServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(RedisEnhancedService.class)
    public RedisEnhancedService redisEnhancedService() {
        return new RedisEnhancedServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(QueueService.class)
    public QueueService queueService() {
        return new RedisQueueServiceImpl(objMapper, redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(SyncLockService.class)
    public SyncLockService syncLockService() {
        return new RedisSyncLockServiceImpl(redissonClient, context);
    }

    @Bean
    @ConditionalOnMissingBean(TokenLimitService.class)
    public TokenLimitService tokenLimitService() {
        return new TokenLimitServiceImpl(redisTemplate, context);
    }

    @Bean
    @ConditionalOnMissingBean(TokenVerifyService.class)
    public TokenVerifyService tokenVerifyService() {
        return new TokenVerifyServiceImpl(context);
    }

    @Bean
    @ConditionalOnMissingBean(TokenService.class)
    public TokenService tokenService() {
        return new TokenServiceImpl(redisTemplate, buildSequence(), objMapper, getAuthConfig(), context);
    }

    @Bean
    @ConditionalOnMissingBean(BaseAuthenticationManagerService.class)
    public BaseAuthenticationManagerService authenticationManagerService() {
        return new AuthenticationManagerServiceImpl(context);
    }

    @Bean
    @ConditionalOnMissingBean(AuthCaptchaService.class)
    public AuthCaptchaService captchaService() {
        final AuthCaptchaServiceImpl impl = new AuthCaptchaServiceImpl(getAuthConfig(), redisTemplate, context);
        impl.afterPropertiesSet();
        return impl;
    }
}
