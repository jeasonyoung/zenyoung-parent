package top.zenyoung.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

/**
 * Jpa-自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@ConditionalOnClass(DataSource.class)
public class JpaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JPAQueryFactory buildJpaQueryFactory(final EntityManager entityManager) {
        log.info("buildJpaQueryFactory...");
        return new JPAQueryFactory(entityManager);
    }
}
