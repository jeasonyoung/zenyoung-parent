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
import org.springframework.web.bind.annotation.*;
import top.zenyoung.common.model.Status;
import top.zenyoung.framework.system.dao.dto.DeptAddDTO;
import top.zenyoung.framework.system.dao.dto.DeptLoadDTO;
import top.zenyoung.framework.system.dao.repository.DeptRepository;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.web.vo.RespDataResult;
import top.zenyoung.web.vo.RespResult;

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
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DeptController extends BaseController {
    private final DeptRepository deptRepository;

    /**
     * 部门-全部数据
     *
     * @param parentDeptId 上级部门ID
     * @return 部门数据集合
     */
    @GetMapping("/all")
    @ApiOperation("1.1.1.部门-全部数据")
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
    @ApiOperation("1.1.2.部门-树数据")
    @PreAuthorize("@ss.hasPermi('system:dept:tree')")
    @ApiImplicitParams(value = {@ApiImplicitParam(name = "parentDeptId", value = "上级部门ID", paramType = "query", dataTypeClass = Long.class)})
    public RespDataResult<DeptTreeResp> getDeptTrees(@RequestParam(required = false) final Long parentDeptId) {
        return buildQuery(() -> buildDeptTree(deptRepository.getDeptWithChildren(parentDeptId)), d -> d);
    }

    private List<DeptTreeResp> buildDeptTree(@Nullable final List<DeptLoadDTO> items) {
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
                roots.forEach(root -> buildDeptChildren(root, parentDeptMaps));
            }
            return roots;
        }
        return null;
    }

    private void buildDeptChildren(@Nonnull final DeptTreeResp root, @Nonnull final Map<Long, List<DeptTreeResp>> parentDeptMaps) {
        //获取子部门集合
        final List<DeptTreeResp> childs = parentDeptMaps.getOrDefault(root.getId(), null);
        if (!CollectionUtils.isEmpty(childs)) {
            root.getChildren().addAll(childs);
            childs.forEach(child -> buildDeptChildren(child, parentDeptMaps));
        }
    }

    /**
     * 部门-加载-数据
     *
     * @param deptId 部门ID
     * @return 部门数据
     */
    @GetMapping("/{deptId}")
    @ApiOperation("1.1.3.部门-加载数据")
    @PreAuthorize("@ss.hasPermi('system:dept:load')")
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
    private static class DeptModify extends DeptAddDTO {
        /**
         * 状态
         */
        @ApiModelProperty("状态")
        private Status status;
    }
}
