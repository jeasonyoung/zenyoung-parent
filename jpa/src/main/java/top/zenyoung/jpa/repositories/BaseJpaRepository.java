package top.zenyoung.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Jpa-数据操作基接口
 *
 * @author young
 */
@NoRepositoryBean
public interface BaseJpaRepository<M extends Serializable, K extends Serializable> extends JpaRepository<M, K>, JpaSpecificationExecutor<M>, QuerydslPredicateExecutor<M> {

}
