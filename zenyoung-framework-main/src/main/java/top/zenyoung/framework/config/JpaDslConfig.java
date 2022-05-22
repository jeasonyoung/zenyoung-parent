package top.zenyoung.framework.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

/**
 * Jpa-dsl配置
 *
 * @author young
 */
@Configuration
public class JpaDslConfig {

    @Bean
    public JPAQueryFactory buildJpaQueryFactory(final EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}
