package top.zenyoung.jpa.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import top.zenyoung.jpa.model.Model;

import java.io.Serializable;

/**
 * Jpa-接口基类
 *
 * @author young
 */
@NoRepositoryBean
public interface BaseJpa<M extends Model<K>, K extends Serializable> extends JpaRepository<M, K>, JpaSpecificationExecutor<M>, QuerydslPredicateExecutor<M> {

}
