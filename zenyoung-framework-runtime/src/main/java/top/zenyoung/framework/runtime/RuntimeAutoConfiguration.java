package top.zenyoung.framework.runtime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.common.sequence.SnowFlake;
import top.zenyoung.framework.auth.AuthProperties;
import top.zenyoung.framework.runtime.config.RuntimeProperties;

/**
 * 运行时模块-自动配置
 *
 * @author young
 */
@Configuration
@ConditionalOnClass(RuntimeProperties.class)
@EnableConfigurationProperties(RuntimeProperties.class)
@ConditionalOnProperty(prefix = "top.zenyoung.runtime", value = "enable", matchIfMissing = true)
public class RuntimeAutoConfiguration {
    @Autowired
    private RuntimeProperties properties;

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
}
