package top.zenyoung.data.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import top.zenyoung.boot.util.SecurityUtils;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.data.jpa.util.SpringContextUtils;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.util.Optional;

/**
 * Jpa-自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@EnableJpaAuditing
@ConditionalOnClass(DataSource.class)
public class JpaAutoConfiguration implements ApplicationContextAware {

    @Override
    public void setApplicationContext(@Nonnull final ApplicationContext context) throws BeansException {
        SpringContextUtils.setContext(context);
    }

    @Autowired
    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    @ConditionalOnMissingBean
    public JPAQueryFactory buildJpaQueryFactory() {
        log.info("buildJpaQueryFactory...");
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditorAware<String> auditorAware() {
        return () -> {
            log.info("auditorAware...");
            return Optional.ofNullable(SecurityUtils.getPrincipal())
                    .map(UserPrincipal::getId)
                    .map(String::valueOf);
        };
    }
}
