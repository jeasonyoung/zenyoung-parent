package top.zenyoung.framework.system.dao.repository.impl;

import com.google.common.base.Strings;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.data.repository.impl.BaseRepositoryImpl;
import top.zenyoung.framework.system.dao.entity.MenuEntity;
import top.zenyoung.framework.system.dao.entity.QMenuEntity;
import top.zenyoung.framework.system.dao.jpa.JpaMenu;
import top.zenyoung.framework.system.dao.repository.MenuRepository;
import top.zenyoung.framework.system.dto.MenuAddDTO;
import top.zenyoung.framework.system.dto.MenuDTO;
import top.zenyoung.framework.system.dto.MenuModifyDTO;
import top.zenyoung.framework.system.dto.MenuQueryDTO;
import top.zenyoung.service.BeanMappingService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单-数据服务接口实现
 *
 * @author young
 */
@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl extends BaseRepositoryImpl implements MenuRepository {
    private final JPAQueryFactory queryFactory;
    private final JpaMenu jpaMenu;

    private final BeanMappingService mappingService;

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public PagingResult<MenuDTO> query(@Nonnull final MenuQueryDTO query) {
        return buildPagingQuery(query, q -> buildDslWhere(new LinkedList<BooleanExpression>() {
            {
                final QMenuEntity qMenuEntity = QMenuEntity.menuEntity;
                //父菜单ID
                final Long pid;
                if ((pid = query.getParentId()) != null && pid > 0) {
                    add(qMenuEntity.parentId.eq(pid));
                }
                //状态
                if (query.getStatus() != null) {
                    add(qMenuEntity.status.eq(query.getStatus()));
                }
                //菜单名称/菜单代码/权限标识
                final String name;
                if (!Strings.isNullOrEmpty(name = query.getName())) {
                    final String like = "%" + name + "%";
                    add(qMenuEntity.name.like(like).or(qMenuEntity.code.like(like)).or(qMenuEntity.perms.like(like)));
                }
            }
        }), jpaMenu, this::convert);
    }

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public MenuDTO getById(@Nonnull final Long id) {
        return convert(jpaMenu.getById(id));
    }

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public List<MenuDTO> getAllByParent(@Nullable final Long parentId) {
        final QMenuEntity qMenuEntity = QMenuEntity.menuEntity;
        JPAQuery<MenuEntity> query = queryFactory.selectFrom(qMenuEntity);
        if (parentId != null && parentId > 0) {
            query = query.where(qMenuEntity.parentId.eq(parentId));
        }
        return query.fetch().stream()
                .map(this::convert)
                .sorted(Comparator.comparingInt(m -> m.getCode()))
                .collect(Collectors.toList());
    }

    private MenuDTO convert(@Nullable final MenuEntity entity) {
        if (entity != null) {
            return mappingService.mapping(entity, MenuDTO.class);
        }
        return null;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Long add(@Nonnull final MenuAddDTO data) {
        final MenuEntity entity = mappingService.mapping(data, MenuEntity.class);
        //主键ID
        entity.setId(sequence.nextId());
        //保存数据
        return jpaMenu.save(entity).getId();
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean update(@Nonnull final Long id, @Nonnull final MenuModifyDTO data) {
        final QMenuEntity qMenuEntity = QMenuEntity.menuEntity;
        return buildDslUpdateClause(queryFactory.update(qMenuEntity))
                //菜单代码
                .add(data.getCode() != null, qMenuEntity.code, data.getCode())
                //菜单名称
                .add(!Strings.isNullOrEmpty(data.getName()), qMenuEntity.name, data.getName())
                //路由地址
                .add(!Strings.isNullOrEmpty(data.getPath()), qMenuEntity.path, data.getPath())
                //组件路径
                .add(!Strings.isNullOrEmpty(data.getComponent()), qMenuEntity.component, data.getComponent())
                //路由参数
                .add(!Strings.isNullOrEmpty(data.getQuery()), qMenuEntity.query, data.getQuery())
                //是否为外链(0:否,1:是)
                .add(data.getIsLink() != null, qMenuEntity.isLink, data.getIsLink())
                //是否缓存(0:不缓存,1:缓存)
                .add(data.getIsCache() != null, qMenuEntity.isCache, data.getIsCache())
                //菜单类型(1:目录,2:菜单,3:按钮)
                .add(data.getType() != null, qMenuEntity.type, data.getType())
                //菜单状态(1:显示,0:隐藏)
                .add(data.getVisible() != null, qMenuEntity.visible, data.getVisible())
                //权限标识
                .add(!Strings.isNullOrEmpty(data.getPerms()), qMenuEntity.perms, data.getPerms())
                //菜单图标
                .add(!Strings.isNullOrEmpty(data.getIcon()), qMenuEntity.icon, data.getIcon())
                //父菜单ID
                .add(data.getParentId() != null, qMenuEntity.parentId, data.getParentId())
                //状态
                .add(data.getStatus() != null, qMenuEntity.status, data.getStatus())
                .execute(qMenuEntity.id.eq(id));
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean delByIds(@Nonnull final Long[] ids) {
        if (ids.length > 0) {
            final QMenuEntity qMenuEntity = QMenuEntity.menuEntity;
            return queryFactory.delete(qMenuEntity)
                    .where(qMenuEntity.id.in(ids))
                    .execute() > 0;
        }
        return false;
    }
}
