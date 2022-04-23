package top.zenyoung.framework.system.util;

import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;
import top.zenyoung.framework.system.dto.DeptAddDTO;
import top.zenyoung.framework.system.dto.DeptLoadDTO;
import top.zenyoung.framework.system.vo.DeptTreeVO;
import top.zenyoung.service.BeanMappingService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 树工具类
 *
 * @author young
 */
public class DeptTreeUtils {

    public static List<DeptTreeVO> buildTrees(@Nullable final List<DeptLoadDTO> items, @Nonnull final BeanMappingService mappingService, @Nullable final List<Long> excludes) {
        if (!CollectionUtils.isEmpty(items)) {
            //父节点集合
            final List<DeptTreeVO> parents = items.stream()
                    .filter(item -> {
                        final Long id = item.getId(), pid = item.getParentId();
                        if (!CollectionUtils.isEmpty(excludes) && id != null && excludes.contains(id)) {
                            return false;
                        }
                        return pid == null || pid == 0;
                    })
                    .map(item -> mappingService.mapping(item, DeptTreeVO.class))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(parents)) {
                //子集合
                final Map<Long, List<DeptTreeVO>> childMaps = items.stream()
                        .filter(item -> {
                            final Long pid = item.getParentId();
                            if (pid != null && pid > 0) {
                                return CollectionUtils.isEmpty(excludes) || !excludes.contains(pid);
                            }
                            return false;
                        })
                        .collect(Collectors.toMap(DeptAddDTO::getParentId,
                                item -> {
                                    final DeptTreeVO child = mappingService.mapping(item, DeptTreeVO.class);
                                    child.setChildren(Lists.newLinkedList());
                                    return Lists.newArrayList(child);
                                },
                                (v1, v2) -> {
                                    v1.addAll(v2);
                                    return v1;
                                }
                        ));
                if (CollectionUtils.isEmpty(childMaps)) {
                    return parents;
                }
                return parents.stream()
                        .peek(p -> buildChildTrees(p, childMaps, excludes))
                        .collect(Collectors.toList());
            }
        }
        return null;
    }

    private static void buildChildTrees(@Nonnull final DeptTreeVO parent, @Nonnull final Map<Long, List<DeptTreeVO>> childMaps, @Nullable final List<Long> excludes) {
        //获取子部门集合
        final List<DeptTreeVO> childs = childMaps.get(parent.getId());
        if (CollectionUtils.isEmpty(childs)) {
            return;
        }
        parent.setChildren(childs.stream()
                .map(child -> {
                    if (!CollectionUtils.isEmpty(excludes) && excludes.contains(child.getId())) {
                        return null;
                    }
                    buildChildTrees(child, childMaps, excludes);
                    return child;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        );
    }
}
