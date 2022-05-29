package top.zenyoung.framework.system.api;

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
import top.zenyoung.framework.system.dao.repository.RoleRepository;
import top.zenyoung.framework.system.dto.RoleAddDTO;
import top.zenyoung.framework.system.dto.RoleDTO;
import top.zenyoung.framework.system.dto.RoleModifyDTO;
import top.zenyoung.framework.system.dto.RoleQueryDTO;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.web.vo.ResultVO;

/**
 * 角色-控制器
 *
 * @author young
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/sys/role")
@Api(value = "1.4-角色管理", tags = "1.系统管理")
public class RoleController extends BaseController {
    private final RoleRepository repository;

    /**
     * 角色管理-查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/query")
    @ApiOperation("1.4.1.角色管理-查询")
    @PreAuthorize("@ss.hasPermi('sys:role:query')")
    public ResultVO<DataResult<RoleDTO>> query(final RoleQueryDTO query) {
        return success(repository.query(query));
    }

    /**
     * 角色管理-加载
     *
     * @param id 角色ID
     * @return 角色数据
     */
    @GetMapping("/{id}")
    @ApiOperation("1.4.2.角色管理-加载")
    @ApiImplicitParam(name = "id", value = "角色ID", paramType = "path", dataTypeClass = Long.class)
    public ResultVO<RoleDTO> getById(@PathVariable final Long id) {
        return success(repository.getById(id));
    }

    /**
     * 角色管理-新增
     *
     * @param data 新增数据
     * @return 新增结果
     */
    @PostMapping
    @ApiOperation("1.4.3.角色管理-新增")
    @PreAuthorize("@ss.hasPermi('sys:role:add')")
    public ResultVO<Long> add(@RequestBody @Validated({Insert.class}) final RoleAddDTO data) {
        return success(repository.add(data));
    }

    /**
     * 角色管理-修改
     *
     * @param id   角色ID
     * @param data 修改数据
     * @return 修改结果
     */
    @PutMapping("/{id}")
    @ApiOperation("1.4.4.角色管理-修改")
    @PreAuthorize("@ss.hasPermi('sys:role:edit')")
    @ApiImplicitParam(name = "id", value = "角色ID", paramType = "path", dataTypeClass = Long.class)
    public ResultVO<Void> modify(@PathVariable final Long id,
                                 @RequestBody @Validated({Modify.class}) final RoleModifyDTO data) {
        final boolean ret = repository.update(id, data);
        return ret ? success() : failed();
    }

    /**
     * 角色管理-删除
     *
     * @param ids 角色ID集合
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @ApiOperation("1.4.5.角色管理-删除")
    @PreAuthorize("@ss.hasPermi('sys:role:del')")
    @ApiImplicitParam(name = "ids", value = "角色ID集合", paramType = "path", dataTypeClass = Long[].class)
    public ResultVO<Void> del(final Long[] ids) {
        final boolean ret = repository.delByIds(ids);
        return ret ? success() : failed();
    }
}
