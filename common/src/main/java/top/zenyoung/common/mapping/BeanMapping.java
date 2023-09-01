package top.zenyoung.common.mapping;

import top.zenyoung.common.paging.PageList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

/**
 * Bean转换
 *
 * @author young
 */
public interface BeanMapping {
    /**
     * bean转换
     *
     * @param data   源数据
     * @param rClass 目标数据类型
     * @param <T>    源数据类型
     * @param <R>    目标数据类型
     * @return 目标数据
     */
    <T, R> R mapping(@Nullable final T data, @Nonnull final Class<R> rClass);

    /**
     * 集合转换
     *
     * @param items  源数据集合
     * @param rClass 目标数据类型
     * @param <T>    源数据类型
     * @param <R>    目标数据类型
     * @return 目标数据集合
     */
    <T, R> List<R> mapping(@Nullable final List<T> items, @Nonnull final Class<R> rClass);

    /**
     * 分页数据转换
     *
     * @param pageList 分页数据集合
     * @param rClass   目标数据类型
     * @param <T>      源数据类型
     * @param <R>      目标数据类型
     * @return 转换后的分页数集合
     */
    <T extends Serializable, R extends Serializable> PageList<R> mapping(@Nullable final PageList<T> pageList, @Nonnull final Class<R> rClass);
}
