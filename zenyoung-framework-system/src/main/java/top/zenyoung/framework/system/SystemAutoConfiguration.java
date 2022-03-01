package top.zenyoung.framework.system;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.zenyoung.common.sequence.Sequence;
import top.zenyoung.common.sequence.SnowFlake;
import top.zenyoung.framework.system.config.SystemProperties;

import javax.persistence.EntityManager;

/**
 * 系统模块自动配置
 *
 * @author young
 */
@Configuration
@ConditionalOnClass(SystemProperties.class)
@ConditionalOnProperty(prefix = "top.zenyoung.system", value = "enable", matchIfMissing = true)
@EnableConfigurationProperties(SystemProperties.class)
public class SystemAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JPAQueryFactory buildJpaQueryFactory(@Autowired final EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public Sequence<Long> buildSnowFlake() {
        return SnowFlake.getInstance();
    }
}
