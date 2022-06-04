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
import top.zenyoung.framework.system.dao.repository.UserRepository;
import top.zenyoung.framework.system.dto.*;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.common.vo.ResultVO;

/**
 * 用户管理-控制器
 *
 * @author young
 */
@RestController
@RequiredArgsConstructor
@ApiSupport(order = 105)
@RequestMapping("/sys/user")
@Api(value = "1.5-用户管理", tags = "1.5系统管理-用户管理")
public class UserController extends BaseController {
    private final UserRepository repository;

    /**
     * 用户管理-查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/query")
    @ApiOperationSupport(order = 1)
    @ApiOperation("1.5.1.用户管理-查询")
    @PreAuthorize("@ss.hasPermi('sys:user:query')")
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
    @ApiOperationSupport(order = 2)
    @ApiOperation("1.5.2.用户管理-加载")
    @ApiImplicitParam(name = "id", value = "用户ID", paramType = "path", dataTypeClass = Long.class)
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
    @ApiOperationSupport(order = 3)
    @ApiOperation("1.5.3.用户管理-新增")
    @PreAuthorize("@ss.hasPermi('sys:user:add')")
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
    @ApiOperationSupport(order = 4)
    @ApiOperation("1.5.4.用户管理-修改")
    @PreAuthorize("@ss.hasPermi('sys:user:edit')")
    @ApiImplicitParam(name = "id", value = "用户ID", paramType = "path", dataTypeClass = Long.class)
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
    @DeleteMapping("/{ids}")
    @ApiOperationSupport(order = 5)
    @ApiOperation("1.5.5.用户管理-删除")
    @PreAuthorize("@ss.hasPermi('sys:user:del')")
    @ApiImplicitParam(name = "ids", value = "用户ID集合", paramType = "path", dataTypeClass = Long[].class)
    public ResultVO<Void> del(@PathVariable final Long[] ids) {
        final boolean ret = repository.delByIds(ids);
        return ret ? success() : failed();
    }

    /**
     * 用户管理-重置密码
     *
     * @param id   用户ID
     * @param data 重置密码数据
     * @return 重置结果
     */
    @PutMapping("/{id}/rest")
    @ApiOperationSupport(order = 6)
    @ApiOperation("1.5.6.用户管理-重置密码")
    @PreAuthorize("@ss.hasPermi('sys:user:reset-pwd')")
    @ApiImplicitParam(name = "id", value = "用户ID", paramType = "path", dataTypeClass = Long.class)
    public ResultVO<Void> restPwd(@PathVariable final Long id, @RequestBody @Validated({Modify.class}) final UserRestPasswordDTO data) {
        final boolean ret = repository.restPassword(id, data);
        return ret ? success() : failed();
    }
}
