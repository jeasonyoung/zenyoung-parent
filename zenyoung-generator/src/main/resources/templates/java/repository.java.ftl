package ${packageName}.dao.repository;

import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.common.paging.PagingResult;

import ${packageName}.dao.dto.${className}DTO;

import javax.annotation.Nonnull;

/**
 * ${comment!}-数据服务接口
 * <#assign lastTime = .now>
 * @author ${author!}
 * @version 1.0
 * @date ${lastTime?iso_utc}
 **/
public interface ${className}Repository {

     /**
      * ${comment!}-分页查询
      *
      * @param pagingQuery 查询条件
      * @return 查询结果
      */
    PagingResult<${className}DTO> query(@Nonnull final PagingQuery<${className}DTO> pagingQuery);

    /**
     * ${comment!}-加载
     *
     * @param id 主键ID
     * @return 加载数据
     */
    ${className}DTO loadById(@Nonnull final Long id);

    /**
     * ${comment!}-新增
     *
     * @param data 新增数据
     * @return 新增ID
     */
    Long add(@Nonnull final ${className}DTO data);

    /**
     * ${comment!}-修改
     *
     * @param data 修改数据
     */
    void modify(@Nonnull final ${className}DTO data);

    /**
     * ${comment!}-删除
     *
     * @param id 主键ID
     */
    void delById(@Nonnull final Long id);
}


