package top.zenyoung.framework.system.api;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.zenyoung.framework.system.dto.DeptAddDTO;
import top.zenyoung.framework.system.dto.DeptLoadDTO;
import top.zenyoung.framework.system.dto.DeptModifyDTO;
import top.zenyoung.framework.system.dao.repository.DeptRepository;
import top.zenyoung.framework.system.util.DeptTreeUtils;
import top.zenyoung.framework.system.vo.DeptTreeVO;
import top.zenyoung.service.BeanMappingService;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.common.valid.Insert;
import top.zenyoung.common.valid.Modify;
import top.zenyoung.web.vo.ResultVO;

import java.util.List;

/**
 * 部门-控制器
 *
 * @author young
 */
@RestController
@Api("1.1.系统管理-部门管理")
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class DeptController extends BaseController {
    private final DeptRepository deptRepository;
    private final BeanMappingService mappingService;

    /**
     * 部门-全部数据
     *
     * @param parentDeptId 上级部门ID
     * @return 部门数据集合
     */
    @GetMapping("/all")
    @ApiOperation("1.1.1.部门-全部")
    @PreAuthorize("@ss.hasPermi('system:dept:all')")
    @ApiImplicitParams(value = {@ApiImplicitParam(name = "parentDeptId", value = "上级部门ID", paramType = "query", dataTypeClass = Long.class)})
    public ResultVO<List<DeptLoadDTO>> getAllDepts(@RequestParam(required = false) final Long parentDeptId) {
        return success(deptRepository.getDeptWithChildren(parentDeptId));
    }

    /**
     * 部门-树数据
     *
     * @param parentDeptId 上级部门ID
     * @return 部门数据集合
     */
    @GetMapping("/tree")
    @ApiOperation("1.1.2.部门-树")
    @PreAuthorize("@ss.hasPermi('system:dept:tree')")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "parentDeptId", value = "上级部门ID", paramType = "query", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "excludes", value = "排除部门及子部门ID集合", paramType = "query", dataTypeClass = Long[].class),
    })
    public ResultVO<List<DeptTreeVO>> getDeptTrees(@RequestParam(required = false) final Long parentDeptId,
                                                   @RequestParam(required = false) final List<Long> excludes) {
        final List<DeptLoadDTO> items = deptRepository.getDeptWithChildren(parentDeptId);
        return success(DeptTreeUtils.buildTrees(items, mappingService, excludes));
    }

    /**
     * 部门-加载-数据
     *
     * @param deptId 部门ID
     * @return 部门数据
     */
    @GetMapping("/{deptId}")
    @ApiOperation("1.1.3.部门-加载")
    @PreAuthorize("@ss.hasPermi('system:dept:load')")
    @ApiImplicitParams(value = {@ApiImplicitParam(name = "deptId", value = "部门ID", paramType = "path", dataTypeClass = Long.class)})
    public ResultVO<DeptLoadDTO> getById(@PathVariable final Long deptId) {
        return success(deptRepository.getDept(deptId));
    }

    /**
     * 部门-新增-数据
     *
     * @param dto 部门数据
     * @return 新增结果
     */
    @PostMapping("/")
    @ApiOperation("1.1.4.部门-新增")
    @PreAuthorize("@ss.hasPermi('system:dept:add')")
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
    @ApiOperation("1.1.5.部门-修改")
    @PreAuthorize("@ss.hasPermi('system:dept:edit')")
    @ApiImplicitParams(value = {@ApiImplicitParam(name = "deptId", value = "部门ID", paramType = "path", dataTypeClass = Long.class)})
    public ResultVO<Void> edit(@PathVariable final Long deptId, @Validated({Modify.class}) @RequestBody final DeptModifyDTO dto) {
        dto.setId(deptId);
        deptRepository.modifyDept(dto);
        return success();
    }

    /**
     * 部门-删除-数据
     *
     * @param deptIds 部门ID集合
     * @return 删除结果
     */
    @DeleteMapping("/{deptIds}")
    @ApiOperation("1.1.6.部门-删除")
    @PreAuthorize("@ss.hasPermi('system:dept:del')")
    @ApiImplicitParams(value = {@ApiImplicitParam(name = "deptIds", value = "部门ID数组", paramType = "path", dataTypeClass = Long[].class)})
    public ResultVO<?> delById(@PathVariable final Long[] deptIds) {
        deptRepository.delDeptByIds(Lists.newArrayList(deptIds));
        return success();
    }
}
