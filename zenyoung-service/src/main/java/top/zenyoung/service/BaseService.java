package top.zenyoung.service;

import top.zenyoung.common.paging.PagingResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

/**
 * 基本服务接口
 *
 * @author young
 */
public interface BaseService {
    /**
     * 数据类型转换
     *
     * @param data   源数据
     * @param tClass 目标类型
     * @param <T>    源数据类型
     * @param <MT>   目标数据类型
     * @return 目标数据
     */
    <T, MT> MT mapping(@Nullable final T data, @Nonnull final Class<MT> tClass);

    /**
     * 数据类型转换
     *
     * @param items  源数据集合
     * @param tClass 目标类型
     * @param <T>    源数据类型
     * @param <MT>   目标数据类型
     * @return 目标数据集合
     */
    <T, MT> List<MT> mapping(@Nullable final List<T> items, @Nonnull final Class<MT> tClass);

    /**
     * 数据类型转换
     *
     * @param pageList 源数据
     * @param tClass   目标类型
     * @param <T>      源数据类型
     * @param <MT>     目标数据类型
     * @return 目标数据集合
     */
    <T extends Serializable, MT extends Serializable> PagingResult<MT> mapping(@Nullable final PagingResult<T> pageList, @Nonnull final Class<MT> tClass);
}
