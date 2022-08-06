package top.zenyoung.orm.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import top.zenyoung.orm.model.BasePO;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;

/**
 * Mapper接口
 *
 * @author young
 */
public interface BaseMapper<PO extends BasePO<ID>, ID extends Serializable> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<PO> {

    /**
     * 获取当前数据模型类型
     *
     * @return 数据模型类型
     */
    @SuppressWarnings({"unchecked"})
    default Class<PO> getModelClass() {
        return (Class<PO>) ReflectionKit.getSuperClassGenericType(getClass(), BaseMapper.class, 0);
    }

    /**
     * 根据条件删除数据
     *
     * @param consumer 删除条件处理
     * @return 删除结果
     */
    default int delete(@Nonnull final Consumer<LambdaQueryWrapper<PO>> consumer) {
        final Class<PO> cls = getModelClass();
        final LambdaQueryWrapper<PO> queryWrapper = Wrappers.lambdaQuery(cls);
        consumer.accept(queryWrapper);
        return delete(queryWrapper);
    }

    /**
     * 更新数据处理
     *
     * @param consumer 更新条件处理
     * @return 更新结果
     */
    default int update(@Nonnull final Consumer<LambdaUpdateWrapper<PO>> consumer) {
        final Class<PO> cls = getModelClass();
        final LambdaUpdateWrapper<PO> updateWrapper = Wrappers.lambdaUpdate(cls);
        consumer.accept(updateWrapper);
        return update(null, updateWrapper);
    }

    /**
     * 根据条件查询第一条数据
     *
     * @param consumer 查询条件处理
     * @return 查询结果
     */
    default PO selectOne(@Nonnull final Consumer<LambdaQueryWrapper<PO>> consumer) {
        final Class<PO> cls = getModelClass();
        final LambdaQueryWrapper<PO> queryWrapper = Wrappers.lambdaQuery(cls);
        consumer.accept(queryWrapper);
        return selectOne(queryWrapper);
    }

    /**
     * 根据条件判断是否存在记录
     *
     * @param consumer 查询条件处理
     * @return 是否存在记录
     */
    default boolean exists(@Nonnull final Consumer<LambdaQueryWrapper<PO>> consumer) {
        final Class<PO> cls = getModelClass();
        final LambdaQueryWrapper<PO> queryWrapper = Wrappers.lambdaQuery(cls);
        consumer.accept(queryWrapper);
        return exists(queryWrapper);
    }

    /**
     * 根据条件查询全部记录
     *
     * @param consumer 查询条件处理
     * @return 查询结果
     */
    default List<PO> selectList(@Nonnull final Consumer<LambdaQueryWrapper<PO>> consumer) {
        final Class<PO> cls = getModelClass();
        final LambdaQueryWrapper<PO> queryWrapper = Wrappers.lambdaQuery(cls);
        consumer.accept(queryWrapper);
        return selectList(queryWrapper);
    }
}
