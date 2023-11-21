package top.zenyoung.jpa.reactive.querydsl;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.ReactiveQuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

@NoRepositoryBean
public interface QuerydslR2dbcPredicateExecutor<M> extends ReactiveQuerydslPredicateExecutor<M> {
    Mono<Page<M>> findAll(@Nonnull final Predicate predicate, @Nonnull final Pageable pageable);
}