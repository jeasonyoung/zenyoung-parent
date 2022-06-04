package top.zenyoung.framework.system.api;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.framework.system.dao.repository.DictRepository;
import top.zenyoung.framework.system.dto.*;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.common.vo.ResultVO;

import java.util.List;

/**
 * 字典-控制器
 *
 * @author young
 */
@RestController
@RequiredArgsConstructor
@ApiSupport(order = 106)
@RequestMapping("/sys/dict")
@Api(value = "1.6-字典管理", tags = "1.6系统管理-字典管理")
public class DictController extends BaseController {
    private final DictRepository repository;

    /**
     * 字典类型管理-查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/query")
    @ApiOperationSupport(order = 1)
    @ApiOperation("1.6.1.字典类型管理-查询")
    @PreAuthorize("@ss.hasPermi('sys:dict:query')")
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
    @ApiOperationSupport(order = 2)
    @ApiOperation("1.6.2.字典类型管理-加载")
    @ApiImplicitParam(name = "typeId", value = "字典类型ID", paramType = "path", dataTypeClass = Long.class)
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
    @ApiOperationSupport(order = 3)
    @ApiOperation("1.6.3.字典类型管理-新增")
    @PreAuthorize("@ss.hasPermi('sys:dict:add')")
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
    @ApiOperationSupport(order = 4)
    @ApiOperation("1.6.4.字典类型管理-修改")
    @PreAuthorize("@ss.hasPermi('sys:dict:edit')")
    @ApiImplicitParam(name = "typeId", value = "字典类型ID", paramType = "path", dataTypeClass = Long.class)
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
    @ApiOperationSupport(order = 5)
    @ApiOperation("1.6.5.字典类型管理-删除")
    @PreAuthorize("@ss.hasPermi('sys:dict:del')")
    @ApiImplicitParam(name = "ids", value = "字典类型ID集合", paramType = "path", dataTypeClass = Long[].class)
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
    @ApiOperationSupport(order = 6)
    @ApiOperation("1.6.6.字典数据管理-加载数据")
    @ApiImplicitParam(name = "dictType", value = "字典类型", paramType = "path", dataTypeClass = String.class)
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
    @ApiOperationSupport(order = 7)
    @ApiOperation("1.6.7.字典数据管理-新增数据")
    @PreAuthorize("@ss.hasPermi('sys:dict:add')")
    @ApiImplicitParam(name = "typeId", value = "字典类型Id", paramType = "path", dataTypeClass = Long.class)
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
    @ApiOperationSupport(order = 8)
    @ApiOperation("1.6.8.字典数据管理-修改数据")
    @PreAuthorize("@ss.hasPermi('sys:dict:edit')")
    @ApiImplicitParam(name = "dataId", value = "字典数据Id", paramType = "path", dataTypeClass = Long.class)
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
    @ApiOperationSupport(order = 9)
    @ApiOperation("1.6.9.字典数据管理-删除数据")
    @PreAuthorize("@ss.hasPermi('sys:dict:del')")
    @ApiImplicitParam(name = "ids", value = "字典数据Id", paramType = "path", dataTypeClass = Long[].class)
    public ResultVO<Boolean> delData(@PathVariable final Long[] ids) {
        return success(repository.delDataByIds(ids));
    }
}
