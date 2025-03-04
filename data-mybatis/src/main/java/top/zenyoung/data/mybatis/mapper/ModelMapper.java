package top.zenyoung.data.mybatis.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Param;
import top.zenyoung.data.entity.Model;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Mapper接口
 *
 * @author young
 */
public interface ModelMapper<M extends Model<K>, K extends Serializable> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<M> {

    /**
     * 获取当前数据模型类型
     *
     * @return 数据模型类型
     */
    @SuppressWarnings({"unchecked"})
    default Class<M> getModelClass() {
        return (Class<M>) ReflectionKit.getSuperClassGenericType(getClass(), ModelMapper.class, 0);
    }

    /**
     * 根据条件删除数据
     *
     * @param consumer 删除条件处理
     * @return 删除结果
     */
    default int delete(@Nonnull final Consumer<LambdaQueryWrapper<M>> consumer) {
        final Class<M> cls = getModelClass();
        final LambdaQueryWrapper<M> queryWrapper = Wrappers.lambdaQuery(cls);
        consumer.accept(queryWrapper);
        return delete(queryWrapper);
    }

    /**
     * 更新数据处理
     *
     * @param consumer 更新条件处理
     * @return 更新结果
     */
    default int update(@Nonnull final Consumer<LambdaUpdateWrapper<M>> consumer) {
        final Class<M> cls = getModelClass();
        final LambdaUpdateWrapper<M> updateWrapper = Wrappers.lambdaUpdate(cls);
        consumer.accept(updateWrapper);
        return update(null, updateWrapper);
    }

    /**
     * 根据条件查询第一条数据
     *
     * @param consumer 查询条件处理
     * @return 查询结果
     */
    default M selectOne(@Nonnull final Consumer<LambdaQueryWrapper<M>> consumer) {
        final Class<M> cls = getModelClass();
        final LambdaQueryWrapper<M> queryWrapper = Wrappers.lambdaQuery(cls);
        consumer.accept(queryWrapper);
        return selectOne(queryWrapper);
    }

    /**
     * 根据条件判断是否存在记录
     *
     * @param consumer 查询条件处理
     * @return 是否存在记录
     */
    default boolean exists(@Nonnull final Consumer<LambdaQueryWrapper<M>> consumer) {
        final Class<M> cls = getModelClass();
        final LambdaQueryWrapper<M> queryWrapper = Wrappers.lambdaQuery(cls);
        consumer.accept(queryWrapper);
        return exists(queryWrapper);
    }

    /**
     * 根据Wrapper条件，查询总记录数
     *
     * @param consumer 查询条件处理
     * @return 总记录数
     */
    default Long selectCount(@Nonnull final Consumer<LambdaQueryWrapper<M>> consumer) {
        final Class<M> cls = getModelClass();
        final LambdaQueryWrapper<M> queryWrapper = Wrappers.lambdaQuery(cls);
        consumer.accept(queryWrapper);
        return selectCount(queryWrapper);
    }

    /**
     * 根据条件查询全部记录
     *
     * @param consumer 查询条件处理
     * @return 查询结果
     */
    default List<M> selectList(@Nonnull final Consumer<LambdaQueryWrapper<M>> consumer) {
        final Class<M> cls = getModelClass();
        final LambdaQueryWrapper<M> queryWrapper = Wrappers.lambdaQuery(cls);
        consumer.accept(queryWrapper);
        return selectList(queryWrapper);
    }

    /**
     * 根据Wrapper条件,查询全部记录
     *
     * @param consumer 查询条件处理
     * @return 查询结果
     */
    default List<Map<String, Object>> selectMaps(@Nonnull final Consumer<LambdaQueryWrapper<M>> consumer) {
        final Class<M> cls = getModelClass();
        final LambdaQueryWrapper<M> queryWrapper = Wrappers.lambdaQuery(cls);
        consumer.accept(queryWrapper);
        return selectMaps(queryWrapper);
    }

    /**
     * 根据Wrapper条件,查询全部记录
     *
     * @param consumer 查询条件处理
     * @return 查询结果
     */
    default List<Object> selectObjs(@Nonnull final Consumer<LambdaQueryWrapper<M>> consumer) {
        final Class<M> cls = getModelClass();
        final LambdaQueryWrapper<M> queryWrapper = Wrappers.lambdaQuery(cls);
        consumer.accept(queryWrapper);
        return selectObjs(queryWrapper);
    }

    /**
     * 根据Wrapper条件,分页查询
     *
     * @param page     分页条件
     * @param consumer 查询条件
     * @param <P>      分页类型
     * @return 查询结果
     */
    default <P extends IPage<M>> P selectPage(@Nonnull final P page, @Nonnull final Consumer<LambdaQueryWrapper<M>> consumer) {
        final Class<M> cls = getModelClass();
        final LambdaQueryWrapper<M> queryWrapper = Wrappers.lambdaQuery(cls);
        consumer.accept(queryWrapper);
        return selectPage(page, queryWrapper);
    }

    /**
     * 根据Wrapper条件,分页查询
     *
     * @param page     分页条件
     * @param consumer 查询条件
     * @param <P>      分页类型
     * @return 查询结果
     */
    default <P extends IPage<Map<String, Object>>> P selectMapsPage(@Nonnull final P page, @Nonnull final Consumer<LambdaQueryWrapper<M>> consumer) {
        final Class<M> cls = getModelClass();
        final LambdaQueryWrapper<M> queryWrapper = Wrappers.lambdaQuery(cls);
        consumer.accept(queryWrapper);
        return selectMapsPage(page, queryWrapper);
    }

    /**
     * 根据ID加载数据(包括被逻辑删除的)
     *
     * @param id ID
     * @return 加载数据
     */
    M selectPhysicalById(@Nonnull final K id);

    /**
     * 根据ID集合查询数据(包括被逻辑删除的)
     *
     * @param ids ID集合
     * @return 数据集合
     */
    List<M> selectPhysicalByIds(@Nonnull @Param(Constants.COLL) final Collection<K> ids);

    /**
     * 批量插入,主键相同则更新
     *
     * @param pos 批量插入数据集合
     * @return 批量插入结果
     */
    int batchAddOrUpdate(@Nonnull @Param(Constants.COLL) final Collection<M> pos);

    /**
     * 物理删除数据
     *
     * @param ids ID集合
     * @return 删除结果
     */
    int physicalDelete(@Nonnull @Param(Constants.COLL) final Collection<K> ids);
}
