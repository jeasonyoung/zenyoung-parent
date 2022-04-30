package top.zenyoung.framework.system.util;

import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import top.zenyoung.framework.system.dto.DeptDTO;
import top.zenyoung.framework.system.vo.DeptTreeVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 部门树-工具类
 *
 * @author young
 */
public class DeptTreeUtils {

    public static List<DeptTreeVO> build(@Nullable final List<DeptDTO> items, @Nullable final List<Long> excludes) {
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        //子部门Map
        final Map<Long, List<DeptTreeVO>> childMaps = items.stream()
                .filter(d -> d.getParentId() != null && d.getParentId() > 0)
                .map(d -> convert(d, excludes))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(DeptDTO::getParentId, Lists::newArrayList,
                        (v1, v2) -> {
                            v1.addAll(v2);
                            return v1;
                        })
                );
        //根部门集合
        final List<DeptTreeVO> parents = items.stream()
                .filter(d -> d.getParentId() == null || d.getParentId() <= 0)
                .map(d -> convert(d, excludes))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(childMaps)) {
            return parents;
        }
        return parents.stream()
                .peek(p -> build(p, childMaps))
                .collect(Collectors.toList());
    }

    private static void build(@Nonnull final DeptTreeVO parent, @Nonnull final Map<Long, List<DeptTreeVO>> childMaps) {
        final Long pid = parent.getId();
        if (pid != null && pid > 0) {
            final List<DeptTreeVO> childs = childMaps.get(pid);
            if (!CollectionUtils.isEmpty(childs)) {
                childs.forEach(p -> build(p, childMaps));
                parent.setChildren(childs);
            }
        }
    }

    private static DeptTreeVO convert(@Nonnull final DeptDTO item, @Nullable final List<Long> excludes) {
        if (!CollectionUtils.isEmpty(excludes) && excludes.contains(item.getId())) {
            return null;
        }
        final DeptTreeVO row = new DeptTreeVO();
        BeanUtils.copyProperties(item, row);
        return row;
    }
}
