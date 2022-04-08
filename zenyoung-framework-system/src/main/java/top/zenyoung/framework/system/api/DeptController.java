package top.zenyoung.framework.system.api;

import com.google.common.collect.Lists;
import io.swagger.annotations.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.zenyoung.common.model.Status;
import top.zenyoung.framework.system.dao.dto.DeptAddDTO;
import top.zenyoung.framework.system.dao.dto.DeptLoadDTO;
import top.zenyoung.framework.system.dao.dto.DeptModifyDTO;
import top.zenyoung.framework.system.dao.repository.DeptRepository;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.web.valid.Insert;
import top.zenyoung.web.valid.Modify;
import top.zenyoung.web.vo.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门-控制器
 *
 * @author young
 */
@Slf4j
@RestController
@Api("1.1.系统管理-部门管理")
@RequestMapping("/system/dept")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DeptController extends BaseController {
    private final DeptRepository deptRepository;

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
    public RespDataResult<DeptLoadResp> getAllDepts(@RequestParam(required = false) final Long parentDeptId) {
        return buildQuery(() -> deptRepository.getDeptWithChildren(parentDeptId), item -> {
            final DeptLoadResp row = new DeptLoadResp();
            BeanUtils.copyProperties(item, row);
            return row;
        });
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
    public RespDataResult<DeptTreeResp> getDeptTrees(
            @RequestParam(required = false) final Long parentDeptId,
            @RequestParam(required = false) final List<Long> excludes
    ) {
        return buildQuery(() -> buildDeptTree(deptRepository.getDeptWithChildren(parentDeptId), excludes), d -> d);
    }

    private List<DeptTreeResp> buildDeptTree(@Nullable final List<DeptLoadDTO> items, @Nullable final List<Long> excludes) {
        if (!CollectionUtils.isEmpty(items)) {
            final Map<Long, List<DeptTreeResp>> parentDeptMaps = items.stream().collect(Collectors.toMap(
                    DeptLoadDTO::getParentId,
                    item -> {
                        final DeptTreeResp row = new DeptTreeResp();
                        BeanUtils.copyProperties(item, row, "children");
                        row.setChildren(Lists.newLinkedList());
                        return Lists.newArrayList(row);
                    },
                    (v1, v2) -> {
                        v1.addAll(v2);
                        return v1;
                    }
            ));
            final List<DeptTreeResp> roots = parentDeptMaps.values().stream().flatMap(Collection::stream)
                    .filter(item -> item.getParentId() == null || item.getParentId() == 0 || !parentDeptMaps.containsKey(item.getParentId()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(roots)) {
                roots.forEach(root -> {
                    if (!CollectionUtils.isEmpty(excludes) && excludes.contains(root.getId())) {
                        return;
                    }
                    buildDeptChildren(root, parentDeptMaps, excludes);
                });
            }
            return roots;
        }
        return null;
    }

    private void buildDeptChildren(@Nonnull final DeptTreeResp root, @Nonnull final Map<Long, List<DeptTreeResp>> parentDeptMaps, @Nullable final List<Long> excludes) {
        //获取子部门集合
        final List<DeptTreeResp> childs = parentDeptMaps.getOrDefault(root.getId(), null);
        if (!CollectionUtils.isEmpty(childs)) {
            root.getChildren().addAll(childs);
            childs.forEach(child -> {
                if (!CollectionUtils.isEmpty(excludes) && excludes.contains(child.getId())) {
                    return;
                }
                buildDeptChildren(child, parentDeptMaps, excludes);
            });
        }
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
    public RespResult<DeptLoadResp> getById(@PathVariable final Long deptId) {
        return action(vod -> {
            final DeptLoadDTO item = deptRepository.getDept(deptId);
            if (item != null) {
                final DeptLoadResp row = new DeptLoadResp();
                BeanUtils.copyProperties(item, row);
                return row;
            }
            return null;
        });
    }

    /**
     * 部门-新增-数据
     *
     * @param addReq 部门数据
     * @return 新增结果
     */
    @PostMapping("/")
    @ApiOperation("1.1.4.部门-新增")
    @PreAuthorize("@ss.hasPermi('system:dept:add')")
    public RespAddResult add(@Validated({Insert.class}) @RequestBody final DeptAddReq addReq) {
        return actionAdd(addReq, deptRepository::addDept);
    }

    /**
     * 部门-修改-数据
     *
     * @param deptId    部门ID
     * @param modifyReq 部门数据
     * @return 修改结果
     */
    @PutMapping("/{deptId}")
    @ApiOperation("1.1.5.部门-修改")
    @PreAuthorize("@ss.hasPermi('system:dept:edit')")
    @ApiImplicitParams(value = {@ApiImplicitParam(name = "deptId", value = "部门ID", paramType = "path", dataTypeClass = Long.class)})
    public RespModifyResult edit(@PathVariable final Long deptId, @Validated({Modify.class}) @RequestBody final DeptModifyReq modifyReq) {
        return actionModify(modifyReq, req -> {
            final DeptModifyDTO data = new DeptModifyDTO();
            BeanUtils.copyProperties(req, data);
            data.setId(deptId);
            deptRepository.modifyDept(data);
        });
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
    public RespDeleteResult delById(@PathVariable final Long[] deptIds) {
        return actionDelete(vod -> deptRepository.delDeptByIds(Lists.newArrayList(deptIds)));
    }

    @ApiModel("部门-加载-响应报文")
    private static class DeptLoadResp extends DeptLoadDTO {

    }

    @Data
    @ApiModel("部门-部门树-响应报文")
    @EqualsAndHashCode(callSuper = true)
    private static class DeptTreeResp extends DeptLoadDTO {
        /**
         * 子部门集合
         */
        private List<DeptTreeResp> children;
    }

    @ApiModel("部门-新增-请求报文")
    private static class DeptAddReq extends DeptAddDTO {

    }

    @Data
    @ApiModel("部门-修改-请求报文")
    @EqualsAndHashCode(callSuper = true)
    private static class DeptModifyReq extends DeptAddDTO {
        /**
         * 状态
         */
        @ApiModelProperty("状态")
        private Status status;
    }
}
