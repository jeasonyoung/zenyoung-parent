package top.zenyoung.framework.system.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.framework.system.dao.repository.DictRepository;
import top.zenyoung.framework.system.dto.*;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.web.vo.ResultVO;

import java.util.List;

/**
 * 字典-控制器
 *
 * @author young
 */
@RestController
@Api("1.5-字典管理")
@RequiredArgsConstructor
@RequestMapping("/system/dict")
public class DictController extends BaseController {
    private final DictRepository repository;

    /**
     * 字典类型管理-查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/query")
    @ApiOperation("1.5.1.字典类型管理-查询")
    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    public ResultVO<DataResult<DictTypeDTO>> queryTypes(final DictTypeQueryDTO query) {
        return success(repository.queryTypes(query));
    }

    /**
     * 字典类型管理-加载
     *
     * @param typeId 字典类型ID
     * @return 加载数据
     */
    @GetMapping("/type/{typeId}")
    @ApiOperation("1.5.2.字典类型管理-加载")
    @PreAuthorize("@ss.hasPermi('system:dict:load')")
    public ResultVO<DictTypeDTO> getTypeById(@PathVariable final Long typeId) {
        return success(repository.getTypeById(typeId));
    }

    /**
     * 字典类型管理-新增
     *
     * @param data 新增数据
     * @return 新增结果
     */
    @PostMapping("/type")
    @ApiOperation("1.5.3.字典类型管理-新增")
    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    public ResultVO<Long> addType(@RequestBody final DictTypeAddDTO data) {
        return success(repository.addType(data));
    }

    /**
     * 字典类型管理-修改
     *
     * @param typeId 字典类型ID
     * @param data   修改数据
     * @return 修改结果
     */
    @PutMapping("/type/{typeId}")
    @ApiOperation("1.5.4.字典类型管理-修改")
    @PreAuthorize("@ss.hasPermi('system:dict:modify')")
    public ResultVO<Boolean> modifyType(@PathVariable final Long typeId, @RequestBody final DictTypeModifyDTO data) {
        return success(repository.updateType(typeId, data));
    }

    /**
     * 字典类型管理-删除
     *
     * @param ids 字典类型ID集合
     * @return 删除结果
     */
    @DeleteMapping("/type/{ids}")
    @ApiOperation("1.5.5.字典类型管理-删除")
    @PreAuthorize("@ss.hasPermi('system:dict:del')")
    public ResultVO<Boolean> delTypes(@PathVariable final Long[] ids) {
        return success(repository.delTypeByIds(ids));
    }

    /**
     * 字典数据管理-加载数据集合
     *
     * @param dictType 字典类型
     * @return 数据集合
     */
    @GetMapping("/{dictType}")
    @ApiOperation("1.5.6.字典数据管理-加载数据")
    @PreAuthorize("@ss.hasPermi('system:dict:data')")
    public ResultVO<DataResult<DictDataDTO>> getDatasByType(@PathVariable final String dictType) {
        return success(DataResult.of(repository.getDataByType(dictType)));
    }

    /**
     * 字典数据管理-批量新增数据
     *
     * @param typeId 字典类型ID
     * @param items  批量新增数据
     * @return 新增结果
     */
    @PostMapping("/type/{typeId}/data")
    @ApiOperation("1.5.7.字典数据管理-新增数据")
    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    public ResultVO<Boolean> addTypeData(@PathVariable final Long typeId, @RequestBody final List<DictDataAddDTO> items) {
        return success(repository.batchAddDatas(typeId, items));
    }

    /**
     * 字典数据管理-修改数据
     *
     * @param dataId 数据ID
     * @param data   修改数据
     * @return 修改结果
     */
    @PutMapping("/data/{dataId}")
    @ApiOperation("1.5.8.字典数据管理-修改数据")
    @PreAuthorize("@ss.hasPermi('system:dict:modify')")
    public ResultVO<Boolean> modifyData(@PathVariable final Long dataId, @RequestBody final DictDataModifyDTO data) {
        return success(repository.updateData(dataId, data));
    }

    /**
     * 字典数据管理-删除数据
     *
     * @param ids 字典数据ID数组
     * @return 删除结果
     */
    @DeleteMapping("/data/{ids}")
    @ApiOperation("1.5.9.字典数据管理-删除数据")
    @PreAuthorize("@ss.hasPermi('system:dict:del')")
    public ResultVO<Boolean> delData(final Long[] ids) {
        return success(repository.delDataByIds(ids));
    }
}
