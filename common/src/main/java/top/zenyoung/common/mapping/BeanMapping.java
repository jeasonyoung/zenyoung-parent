package top.zenyoung.common.mapping;

import top.zenyoung.common.exception.ServiceException;
import top.zenyoung.common.model.ResultCode;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.vo.ResultVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Bean转换
 *
 * @author young
 */
public interface BeanMapping {
    /**
     * bean转换
     *
     * @param data 源数据
     * @param cls  目标数据类型
     * @param <T>  源数据类型
     * @param <R>  目标数据类型
     * @return 目标数据
     */
    <T, R> R mapping(@Nullable final T data, @Nonnull final Class<R> cls);

    /**
     * 集合转换
     *
     * @param items 源数据集合
     * @param cls   目标数据类型
     * @param <T>   源数据类型
     * @param <R>   目标数据类型
     * @return 目标数据集合
     */
    <T, R> List<R> mapping(@Nullable final List<T> items, @Nonnull final Class<R> cls);

    /**
     * 分页数据转换
     *
     * @param pageList 分页数据集合
     * @param cls      目标数据类型
     * @param <T>      源数据类型
     * @param <R>      目标数据类型
     * @return 转换后的分页数集合
     */
    <T extends Serializable, R extends Serializable> PageList<R> mapping(@Nullable final PageList<T> pageList, @Nonnull final Class<R> cls);

    /**
     * 数据校验转换处理
     *
     * @param ret     源数据
     * @param handler 数据处理
     * @param <T>     源数据类型
     * @param <R>     目标数据类型
     * @return 目标数据
     */
    default <T extends Serializable, R extends Serializable> R validData(@Nullable final ResultVO<? extends T> ret, @Nonnull final Function<T, R> handler) {
        return Optional.ofNullable(ret)
                .map(t -> {
                    final boolean ok = t.getCode() == ResultCode.SUCCESS.getVal();
                    if (!ok) {
                        throw new ServiceException(t.getCode(), t.getMessage());
                    }
                    return handler.apply(t.getData());
                })
                .orElse(null);
    }

    /**
     * 数据校验转换处理
     *
     * @param ret 结果数据
     * @param cls 目标数据类型
     * @param <T> 源数据类型
     * @param <R> 目标数据类型
     * @return 目标数据
     */
    default <T extends Serializable, R extends Serializable> R validData(@Nullable final ResultVO<? extends T> ret, @Nonnull final Class<R> cls) {
        return validData(ret, item -> mapping(item, cls));
    }

    /**
     * 数据校验转换处理
     *
     * @param ret 源数据
     * @param <T> 数据类型
     * @return 目标数据
     */
    default <T extends Serializable> T validData(@Nullable final ResultVO<? extends T> ret) {
        return validData(ret, t -> t);
    }

    /**
     * 数据弹出转换处理
     *
     * @param ret     源数据
     * @param handler 数据转换处理
     * @param <T>     源数据类型
     * @param <R>     目标数据
     * @return 目标数据
     */
    default <T extends Serializable, R extends Serializable> R ejectionData(@Nullable final ResultVO<? extends T> ret,
                                                                            @Nonnull final Function<T, R> handler) {
        return Optional.ofNullable(ret)
                .filter(t -> t.getCode() == ResultCode.SUCCESS.getVal())
                .map(t -> handler.apply(t.getData()))
                .orElse(null);
    }

    /**
     * 数据弹出转换处理
     *
     * @param ret 源数据
     * @param cls 目标数据
     * @param <T> 源数据类型
     * @param <R> 目标数据
     * @return 目标数据
     */
    default <T extends Serializable, R extends Serializable> R ejectionData(@Nullable final ResultVO<? extends T> ret, @Nonnull final Class<R> cls) {
        return ejectionData(ret, item -> mapping(item, cls));
    }

    /**
     * 数据弹出处理
     *
     * @param ret 源数据
     * @param <T> 数据类型
     * @return 目标数据
     */
    default <T extends Serializable> T ejectionData(@Nullable final ResultVO<? extends T> ret) {
        return ejectionData(ret, t -> t);
    }
}
