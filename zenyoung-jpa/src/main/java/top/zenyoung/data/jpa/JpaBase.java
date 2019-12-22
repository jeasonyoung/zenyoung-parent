package top.zenyoung.data.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * jpa-基类接口
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/10/18 2:49 下午
 **/
@NoRepositoryBean
public interface JpaBase<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T>, QuerydslPredicateExecutor<T> {

}
