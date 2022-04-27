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
import top.zenyoung.framework.system.dao.repository.UserRepository;
import top.zenyoung.framework.system.dto.UserAddDTO;
import top.zenyoung.framework.system.dto.UserDTO;
import top.zenyoung.framework.system.dto.UserModifyDTO;
import top.zenyoung.framework.system.dto.UserQueryDTO;
import top.zenyoung.service.BeanMappingService;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.web.vo.ResultVO;

/**
 * 用户管理-控制器
 *
 * @author young
 */
@RestController
@Api("1.2-用户管理")
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController extends BaseController {
    private final UserRepository repository;
    private final BeanMappingService mappingService;

    /**
     * 用户管理-查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/query")
    @ApiOperation("1.2.1.用户管理-查询")
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    public ResultVO<DataResult<UserDTO>> query(final UserQueryDTO query) {
        return success(repository.query(query));
    }

    /**
     * 用户管理-加载
     *
     * @param id 用户ID
     * @return 用户数据
     */
    @GetMapping("/{id}")
    @ApiOperation("1.2.2.用户管理-加载")
    @PreAuthorize("@ss.hasPermi('system:user:load')")
    public ResultVO<UserDTO> getById(@PathVariable final Long id) {
        return success(repository.getById(id));
    }

    /**
     * 用户管理-新增
     *
     * @param data 新增数据
     * @return 新增结果
     */
    @PostMapping
    @ApiOperation("1.2.3.用户管理-新增")
    @PreAuthorize("@ss.hasPermi('system:user:add')")
    public ResultVO<Long> add(@RequestBody @Validated({Insert.class}) final UserAddDTO data) {
        return success(repository.add(data));
    }

    /**
     * 用户管理-修改
     *
     * @param id   用户ID
     * @param data 修改数据
     * @return 修改结果
     */
    @PutMapping("/{id}")
    @ApiOperation("1.2.4.用户管理-修改")
    @ApiImplicitParam(name = "id", value = "用户ID", paramType = "path", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermi('system:user:modify')")
    public ResultVO<Void> modify(@PathVariable final Long id, @RequestBody @Validated({Modify.class}) final UserModifyDTO data) {
        final boolean ret = repository.update(id, data);
        return ret ? success() : failed();
    }

    /**
     * 用户管理-删除
     *
     * @param ids 用户ID集合
     * @return 删除结果
     */
    @PutMapping("/{ids}")
    @ApiOperation("1.2.5.用户管理-删除")
    @ApiImplicitParam(name = "ids", value = "用户ID集合", paramType = "path", dataTypeClass = Long[].class)
    @PreAuthorize("@ss.hasPermi('system:user:del')")
    public ResultVO<Void> del(@PathVariable final Long[] ids) {
        final boolean ret = repository.delByIds(ids);
        return ret ? success() : failed();
    }
}
