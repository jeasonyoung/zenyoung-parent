package top.zenyoung.framework.system.util;

import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import top.zenyoung.framework.system.dto.MenuDTO;
import top.zenyoung.framework.system.vo.MenuTreeVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单树工具类
 *
 * @author young
 */
public class MenuTreeUtils {

    public static List<MenuTreeVO> build(@Nullable final List<MenuDTO> items) {
        if (CollectionUtils.isEmpty(items)) {
            return Lists.newArrayList();
        }
        //子菜单Map
        final Map<Long, List<MenuTreeVO>> childMaps = items.stream()
                .filter(m -> m.getParentId() != null && m.getParentId() > 0)
                .map(MenuTreeUtils::convert)
                .collect(Collectors.toMap(MenuDTO::getParentId, Lists::newArrayList, (v1, v2) -> {
                    v1.addAll(v2);
                    return v1;
                }));
        //根菜单集合
        final List<MenuTreeVO> parents = items.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() <= 0)
                .map(MenuTreeUtils::convert)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(childMaps)) {
            return parents;
        }
        return parents.stream()
                .peek(p -> build(p, childMaps))
                .collect(Collectors.toList());
    }

    private static void build(@Nonnull final MenuTreeVO parent, @Nonnull final Map<Long, List<MenuTreeVO>> childMaps) {
        final Long pid = parent.getId();
        if (pid != null && pid > 0) {
            final List<MenuTreeVO> childs = childMaps.get(pid);
            if (!CollectionUtils.isEmpty(childs)) {
                childs.forEach(p -> build(p, childMaps));
                parent.setChildren(childs);
            }
        }
    }

    private static MenuTreeVO convert(@Nonnull final MenuDTO item) {
        final MenuTreeVO row = new MenuTreeVO();
        BeanUtils.copyProperties(item, row);
        return row;
    }
}
