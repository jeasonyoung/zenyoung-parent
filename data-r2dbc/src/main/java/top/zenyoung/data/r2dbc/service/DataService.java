package top.zenyoung.data.r2dbc.service;

import reactor.core.publisher.Mono;
import top.zenyoung.common.mapping.BeanMapping;
import top.zenyoung.data.entity.Model;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collection;

/**
 * jpa-reative 数据服务接口
 *
 * @param <M> 数据实体类型
 * @param <K> 数据主键类型
 * @author young
 */
public interface DataService<M extends Model<K>, K extends Serializable> extends BeanMapping {
    /**
     * 根据ID加载数据
     *
     * @param id ID
     * @return 加载数据
     */
    @Nonnull
    Mono<M> findById(@Nonnull final K id);

    /**
     * 新增
     *
     * @param po 新增数据
     * @return 新增结果
     */
    @Nonnull
    Mono<Boolean> add(@Nonnull final M po);

    /**
     * 批量新增
     *
     * @param pos 新增数据集合
     * @return 新增结果
     */
    @Nonnull
    Mono<Boolean> batchAdd(@Nonnull final Collection<M> pos);

    /**
     * 根据主键ID删除
     *
     * @param id 主键ID
     * @return 删除
     */
    @Nonnull
    Mono<Boolean> delete(@Nonnull final K id);

    /**
     * 根据主键ID集合删除
     *
     * @param ids 主键ID集合
     * @return 删除
     */
    @Nonnull
    Mono<Boolean> delete(@Nonnull final Collection<K> ids);
}