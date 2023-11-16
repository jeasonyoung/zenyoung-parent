package top.zenyoung.jpa.reactive.repositories;

import org.springframework.data.querydsl.ReactiveQuerydslPredicateExecutor;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Repository 数据存储接口
 *
 * @author young
 */
@NoRepositoryBean
public interface BaseRepository<M, K> extends R2dbcRepository<M, K>, ReactiveQuerydslPredicateExecutor<M> {

}
