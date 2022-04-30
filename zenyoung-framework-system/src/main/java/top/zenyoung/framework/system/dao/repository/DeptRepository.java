package top.zenyoung.framework.system.dao.repository;

import top.zenyoung.framework.system.dto.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 部门-数据操作接口
 *
 * @author young
 */
public interface DeptRepository {

    /**
     * 部门-加载部门及子部门集合数据
     *
     * @param parentDeptId 上级部门ID
     * @return 全部数据
     */
    List<DeptDTO> getDeptWithChildren(@Nullable final Long parentDeptId);

    /**
     * 部门-加载
     *
     * @param id 部门ID
     * @return 加载结果
     */
    DeptDTO getDept(@Nonnull final Long id);

    /**
     * 部门-基础信息
     *
     * @param id 部门ID
     * @return 基础信息
     */
    DeptInfoDTO getDeptInfoById(@Nonnull final Long id);

    /**
     * 部门-新增
     *
     * @param data 新增数据
     * @return 新增结果
     */
    Long addDept(@Nonnull final DeptAddDTO data);

    /**
     * 部门-修改
     *
     * @param id   部门ID
     * @param data 修改数据
     */
    boolean modifyDept(@Nonnull final Long id, @Nonnull final DeptModifyDTO data);

    /**
     * 部门-删除
     *
     * @param ids 部门ID集合
     * @return 删除结果
     */
    int delDeptByIds(@Nonnull final List<Long> ids);
}
