package top.zenyoung.framework.system.api;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.common.valid.Insert;
import top.zenyoung.common.valid.Modify;
import top.zenyoung.framework.system.dao.repository.ConfigRepository;
import top.zenyoung.framework.system.dto.ConfigAddDTO;
import top.zenyoung.framework.system.dto.ConfigDTO;
import top.zenyoung.framework.system.dto.ConfigModifyDTO;
import top.zenyoung.framework.system.dto.ConfigQueryDTO;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.common.vo.ResultVO;

/**
 * 参数配置-控制器
 *
 * @author young
 */
@RestController
@ApiSupport(order = 107)
@RequiredArgsConstructor
@RequestMapping("/sys/config")
@Api(value = "1.7-参数管理", tags = "1.7系统管理-参数管理")
public class ConfigController extends BaseController {
    private final ConfigRepository repository;

    /**
     * 参数配置-查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/query")
    @ApiOperationSupport(order = 1)
    @ApiOperation("1.7.1.参数管理-查询")
    @PreAuthorize("@ss.hasPermi('sys:config:query')")
    public ResultVO<DataResult<ConfigDTO>> query(final ConfigQueryDTO query) {
        return success(repository.query(query));
    }

    /**
     * 参数配置-加载
     *
     * @param id 参数配置ID
     * @return 参数配置
     */
    @GetMapping("/{id}")
    @ApiOperationSupport(order = 2)
    @ApiOperation("1.7.2.参数管理-加载")
    @ApiImplicitParam(name = "id", value = "参数配置ID", paramType = "path", dataTypeClass = Long.class)
    public ResultVO<ConfigDTO> getById(@PathVariable final Long id) {
        return success(repository.getById(id));
    }

    /**
     * 参数配置-新增
     *
     * @param data 新增数据
     * @return 新增结果
     */
    @PostMapping
    @ApiOperationSupport(order = 3)
    @ApiOperation("1.7.3.参数管理-新增")
    @PreAuthorize("@ss.hasPermi('sys:config:add')")
    public ResultVO<Long> add(@RequestBody @Validated({Insert.class}) final ConfigAddDTO data) {
        return success(repository.add(data));
    }

    /**
     * 参数配置-修改
     *
     * @param id   参数配置ID
     * @param data 修改数据
     * @return 修改结果
     */
    @PutMapping("/{id}")
    @ApiOperationSupport(order = 4)
    @ApiOperation("1.7.4.参数管理-修改")
    @PreAuthorize("@ss.hasPermi('sys:config:edit')")
    @ApiImplicitParam(name = "id", value = "参数配置ID", paramType = "path", dataTypeClass = Long.class)
    public ResultVO<Void> modify(@PathVariable final Long id, @RequestBody @Validated({Modify.class}) final ConfigModifyDTO data) {
        final boolean ret = repository.update(id, data);
        return ret ? success() : failed();
    }

    /**
     * 参数配置-删除
     *
     * @param ids 参数配置ID集合
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @ApiOperationSupport(order = 5)
    @ApiOperation("1.7.5.参数管理-删除")
    @PreAuthorize("@ss.hasPermi('sys:config:del')")
    @ApiImplicitParam(name = "ids", value = "参数配置ID集合", paramType = "path", dataTypeClass = Long[].class)
    public ResultVO<Void> del(@PathVariable final Long[] ids) {
        final boolean ret = repository.delByIds(ids);
        return ret ? success() : failed();
    }
}
