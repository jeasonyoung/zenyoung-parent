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
import top.zenyoung.framework.system.dao.repository.MenuRepository;
import top.zenyoung.framework.system.dto.MenuAddDTO;
import top.zenyoung.framework.system.dto.MenuDTO;
import top.zenyoung.framework.system.dto.MenuModifyDTO;
import top.zenyoung.framework.system.dto.MenuQueryDTO;
import top.zenyoung.framework.system.util.MenuTreeUtils;
import top.zenyoung.framework.system.vo.MenuTreeVO;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.web.vo.ResultVO;

/**
 * 菜单-控制器
 *
 * @author young
 */
@RestController
@Api("1.4-菜单管理")
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class MenuController extends BaseController {
    private final MenuRepository repository;

    /**
     * 菜单管理-查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/query")
    @ApiOperation("1.4.1.菜单管理-查询")
    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    public ResultVO<DataResult<MenuDTO>> query(final MenuQueryDTO query) {
        return success(repository.query(query));
    }

    /**
     * 菜单管理-加载
     *
     * @param id 菜单ID
     * @return 岗位数据
     */
    @GetMapping("/{id}")
    @ApiOperation("1.4.2.岗位管理-加载")
    @PreAuthorize("@ss.hasPermi('system:menu:load')")
    public ResultVO<MenuDTO> getById(@PathVariable final Long id) {
        return success(repository.getById(id));
    }

    /**
     * 菜单管理-菜单树
     *
     * @param parentId 父菜单树ID
     * @return 菜单树集合
     */
    @GetMapping("/tree")
    @ApiOperation("1.4.3.岗位管理-菜单树")
    @PreAuthorize("@ss.hasPermi('system:menu:tree')")
    public ResultVO<DataResult<MenuTreeVO>> getAll(@RequestParam(required = false) final Long parentId) {
        return success(DataResult.of(MenuTreeUtils.build(repository.getAllByParent(parentId))));
    }

    /**
     * 菜单管理-新增
     *
     * @param data 新增数据
     * @return 新增结果
     */
    @PostMapping
    @ApiOperation("1.4.4.菜单管理-新增")
    @PreAuthorize("@ss.hasPermi('system:menu:add')")
    public ResultVO<Long> add(@RequestBody @Validated({Insert.class}) final MenuAddDTO data) {
        return success(repository.add(data));
    }

    /**
     * 菜单管理-修改
     *
     * @param id   岗位ID
     * @param data 修改数据
     * @return 修改结果
     */
    @PutMapping("/{id}")
    @ApiOperation("1.4.5.菜单管理-修改")
    @ApiImplicitParam(name = "id", value = "菜单ID", paramType = "path", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermi('system:menu:modify')")
    public ResultVO<Void> modify(@PathVariable final Long id, @RequestBody @Validated({Modify.class}) final MenuModifyDTO data) {
        final boolean ret = repository.update(id, data);
        return ret ? success() : failed();
    }

    /**
     * 菜单管理-删除
     *
     * @param ids 菜单ID集合
     * @return 删除结果
     */
    @PutMapping("/{ids}")
    @ApiOperation("1.4.6.菜单管理-删除")
    @ApiImplicitParam(name = "ids", value = "菜单ID集合", paramType = "path", dataTypeClass = Long[].class)
    @PreAuthorize("@ss.hasPermi('system:menu:del')")
    public ResultVO<Void> del(@PathVariable final Long[] ids) {
        final boolean ret = repository.delByIds(ids);
        return ret ? success() : failed();
    }
}