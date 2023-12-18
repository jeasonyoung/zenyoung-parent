package top.zenyoung.data.r2dbc.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.NoRepositoryBean;
import top.zenyoung.data.entity.Model;

import java.io.Serializable;

/**
 * Jpa-数据操作基接口
 *
 * @author young
 */
@NoRepositoryBean
public interface DataRepository<M extends Model<K>, K extends Serializable> extends R2dbcRepository<M, K> {

}
