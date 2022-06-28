package top.zenyoung.common.mapping;

import top.zenyoung.common.paging.PageList;

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
     * @param data    源数据
     * @param mtClass 目标数据类型
     * @param <T>     源数据类型
     * @param <MT>    目标数据类型
     * @return 目标数据
     */
    <T, MT> MT mapping(final T data, final Class<MT> mtClass);

    /**
     * 集合转换
     *
     * @param items   源数据集合
     * @param mtClass 目标数据类型
     * @param <T>     源数据类型
     * @param <MT>    目标数据类型
     * @return 目标数据集合
     */
    <T, MT> List<MT> mapping(final List<T> items, final Class<MT> mtClass);

    /**
     * 分页数据转换
     *
     * @param pageList 分页数据集合
     * @param mtClass  目标数据类型
     * @param <T>      源数据类型
     * @param <MT>     目标数据类型
     * @return 转换后的分页数集合
     */
    <T, MT> PageList<MT> mapping(final PageList<T> pageList, final Class<MT> mtClass);
}
