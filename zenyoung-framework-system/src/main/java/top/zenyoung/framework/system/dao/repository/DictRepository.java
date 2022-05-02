package top.zenyoung.framework.system.dao.repository;

import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.framework.system.dto.*;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 字典-数据服务接口
 *
 * @author young
 */
public interface DictRepository {

    /**
     * 字典类型-分页查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PagingResult<DictTypeDTO> queryTypes(@Nonnull final DictTypeQueryDTO query);

    /**
     * 字典类型-加载
     *
     * @param typeId 字典类型ID
     * @return 字典类型数据
     */
    DictTypeDTO getTypeById(@Nonnull final Long typeId);

    /**
     * 字典类型-新增
     *
     * @param data 新增数据
     * @return 新增结果
     */
    Long addType(@Nonnull final DictTypeAddDTO data);

    /**
     * 字典类型-修改
     *
     * @param typeId 字典类型ID
     * @param data   修改数据
     * @return 修改结果
     */
    boolean updateType(@Nonnull final Long typeId, @Nonnull final DictTypeModifyDTO data);

    /**
     * 字典类型-删除
     *
     * @param typeIds 字典类型ID集合
     * @return 删除结果
     */
    boolean delTypeByIds(@Nonnull final Long[] typeIds);

    /**
     * 字典数据-根据字典类型
     *
     * @param dictType 字典类型
     * @return 字典数据集合
     */
    List<DictDataDTO> getDataByType(@Nonnull final String dictType);

    /**
     * 字典数据-批量新增
     *
     * @param typeId 字典类型ID
     * @param items  字典数据集合
     * @return 批量新增结果
     */
    boolean batchAddDatas(@Nonnull final Long typeId, @Nonnull final List<DictDataAddDTO> items);

    /**
     * 字典数据-修改
     *
     * @param dataId 字典数据ID
     * @param data   修改数据
     * @return 修改结果
     */
    boolean updateData(@Nonnull final Long dataId, @Nonnull final DictDataModifyDTO data);

    /**
     * 字典数据-删除
     *
     * @param dataIds 字典数据ID集合
     * @return 删除结果
     */
    boolean delDataByIds(@Nonnull final Long[] dataIds);
}
