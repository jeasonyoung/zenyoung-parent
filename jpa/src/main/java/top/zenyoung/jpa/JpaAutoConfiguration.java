package top.zenyoung.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.common.util.SecurityUtils;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
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
public class JpaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JPAQueryFactory buildJpaQueryFactory(@Nonnull final EntityManager entityManager) {
        log.info("buildJpaQueryFactory...");
        return new JPAQueryFactory(entityManager);
    }

    @Configuration
    public static class AuditingAuditorAware implements AuditorAware<String> {

        @Nonnull
        @Override
        public Optional<String> getCurrentAuditor() {
            return Optional.ofNullable(SecurityUtils.getPrincipal())
                    .map(UserPrincipal::getId)
                    .map(String::valueOf);
        }
    }
}
