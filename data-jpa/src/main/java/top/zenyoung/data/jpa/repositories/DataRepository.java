package top.zenyoung.data.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import top.zenyoung.data.entity.Model;

import java.io.Serializable;

/**
 * Jpa-数据操作基接口
 *
 * @author young
 */
@NoRepositoryBean
public interface DataRepository<M extends Model<K>, K extends Serializable> extends JpaRepository<M, K>, JpaSpecificationExecutor<M>, QuerydslPredicateExecutor<M> {

}
