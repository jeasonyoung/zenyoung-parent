package top.zenyoung.framework.system.api;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.zenyoung.common.valid.Insert;
import top.zenyoung.common.valid.Modify;
import top.zenyoung.framework.system.dao.repository.DeptRepository;
import top.zenyoung.framework.system.dto.DeptAddDTO;
import top.zenyoung.framework.system.dto.DeptDTO;
import top.zenyoung.framework.system.dto.DeptModifyDTO;
import top.zenyoung.framework.system.util.DeptTreeUtils;
import top.zenyoung.framework.system.vo.DeptTreeVO;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.common.vo.ResultVO;

import java.util.List;

/**
 * 部门-控制器
 *
 * @author young
 */
@RestController
@ApiSupport(order = 101)
@RequiredArgsConstructor
@RequestMapping("/sys/dept")
@Api(value = "1.1.部门管理", tags = "1.1系统管理-部门管理")
public class DeptController extends BaseController {
    private final DeptRepository deptRepository;

    /**
     * 部门-全部数据
     *
     * @param pid 上级部门ID
     * @return 部门数据集合
     */
    @GetMapping("/all")
    @ApiOperationSupport(order = 1)
    @ApiOperation("1.1.1.部门管理-全部")
    @ApiImplicitParam(name = "pid", value = "上级部门ID", paramType = "query", dataTypeClass = Long.class)
    public ResultVO<List<DeptTreeVO>> getAllDepts(final Long pid) {
        return success(DeptTreeUtils.build(deptRepository.getDeptWithChildren(pid), null));
    }

    /**
     * 部门-树数据
     *
     * @param pid      上级部门ID
     * @param excludes 须排除的部门ID集合
     * @return 部门数据集合
     */
    @GetMapping("/tree")
    @ApiOperationSupport(order = 2)
    @ApiOperation("1.1.2.部门管理-部门树")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "pid", value = "上级部门ID", paramType = "query", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "excludes", value = "排除部门及子部门ID集合", paramType = "query", dataTypeClass = Long[].class),
    })
    public ResultVO<List<DeptTreeVO>> getDeptTrees(@RequestParam(required = false) final Long pid,
                                                   @RequestParam(required = false) final List<Long> excludes) {
        return success(DeptTreeUtils.build(deptRepository.getDeptWithChildren(pid), excludes));
    }

    /**
     * 部门-加载-数据
     *
     * @param deptId 部门ID
     * @return 部门数据
     */
    @GetMapping("/{deptId}")
    @ApiOperationSupport(order = 3)
    @ApiOperation("1.1.3.部门管理-加载")
    @ApiImplicitParam(name = "deptId", value = "部门ID", paramType = "path", dataTypeClass = Long.class)
    public ResultVO<DeptDTO> getById(@PathVariable final Long deptId) {
        return success(deptRepository.getDept(deptId));
    }

    /**
     * 部门-新增-数据
     *
     * @param dto 部门数据
     * @return 新增结果
     */
    @PostMapping("/")
    @ApiOperationSupport(order = 4)
    @ApiOperation("1.1.4.部门管理-新增")
    @PreAuthorize("@ss.hasPermi('sys:dept:add')")
    public ResultVO<Long> add(@Validated({Insert.class}) @RequestBody final DeptAddDTO dto) {
        return success(deptRepository.addDept(dto));
    }

    /**
     * 部门-修改-数据
     *
     * @param deptId 部门ID
     * @param dto    部门数据
     * @return 修改结果
     */
    @PutMapping("/{deptId}")
    @ApiOperationSupport(order = 5)
    @ApiOperation("1.1.5.部门管理-修改")
    @PreAuthorize("@ss.hasPermi('sys:dept:edit')")
    @ApiImplicitParam(name = "deptId", value = "部门ID", paramType = "path", dataTypeClass = Long.class)
    public ResultVO<Void> edit(@PathVariable final Long deptId, @Validated({Modify.class}) @RequestBody final DeptModifyDTO dto) {
        final boolean ret = deptRepository.modifyDept(deptId, dto);
        return ret ? success() : failed();
    }

    /**
     * 部门-删除-数据
     *
     * @param deptIds 部门ID集合
     * @return 删除结果
     */
    @DeleteMapping("/{deptIds}")
    @ApiOperationSupport(order = 6)
    @ApiOperation("1.1.6.部门管理-删除")
    @PreAuthorize("@ss.hasPermi('sys:dept:del')")
    @ApiImplicitParam(name = "deptIds", value = "部门ID数组", paramType = "path", dataTypeClass = Long[].class)
    public ResultVO<?> delById(@PathVariable final Long[] deptIds) {
        final boolean ret = deptRepository.delDeptByIds(deptIds);
        return ret ? success() : failed();
    }
}
