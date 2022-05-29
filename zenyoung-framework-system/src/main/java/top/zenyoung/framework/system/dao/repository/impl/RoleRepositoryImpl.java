package top.zenyoung.framework.system.dao.repository.impl;

import com.google.common.base.Strings;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.data.repository.impl.BaseRepositoryImpl;
import top.zenyoung.framework.system.dao.entity.QRoleEntity;
import top.zenyoung.framework.system.dao.entity.RoleEntity;
import top.zenyoung.framework.system.dao.jpa.JpaRole;
import top.zenyoung.framework.system.dao.repository.RoleRepository;
import top.zenyoung.framework.system.dto.RoleAddDTO;
import top.zenyoung.framework.system.dto.RoleDTO;
import top.zenyoung.framework.system.dto.RoleModifyDTO;
import top.zenyoung.framework.system.dto.RoleQueryDTO;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.Objects;

/**
 * 角色-数据服务接口实现
 *
 * @author young
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl extends BaseRepositoryImpl implements RoleRepository {
    private final JpaRole jpaRole;

    @Override
    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    public PagingResult<RoleDTO> query(@Nonnull final RoleQueryDTO query) {
        return buildPagingQuery(query, q -> buildDslWhere(new LinkedList<BooleanExpression>() {
            {
                final QRoleEntity qEntity = QRoleEntity.roleEntity;
                //状态
                if (Objects.nonNull(q.getStatus())) {
                    add(qEntity.status.eq(q.getStatus()));
                }
                //角色代码/名称/简称
                final String name;
                if (!Strings.isNullOrEmpty(name = query.getName())) {
                    final String like = "%" + name + "%";
                    add(qEntity.code.like(like).or(qEntity.name.like(like)).or(qEntity.abbr.like(like)));
                }
            }
        }), jpaRole, entity -> mapping(entity, RoleDTO.class));
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    public RoleDTO getById(@Nonnull final Long id) {
        return mapping(jpaRole.getOne(id), RoleDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Long add(@Nonnull final RoleAddDTO data) {
        final QRoleEntity qEntity = QRoleEntity.roleEntity;
        if (jpaRole.exists(qEntity.name.eq(data.getName()).or(qEntity.abbr.eq(data.getAbbr())))) {
            throw new IllegalArgumentException("角色名称或别称已存在!");
        }
        final RoleEntity entity = mapping(data, RoleEntity.class);
        return jpaRole.save(entity).getId();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean update(@Nonnull final Long id, @Nonnull final RoleModifyDTO data) {
        final QRoleEntity qEntity = QRoleEntity.roleEntity;
        return buildDslUpdateClause(queryFactory.update(qEntity))
                //角色代码(排序)
                .add(Objects.nonNull(data.getCode()), qEntity.code, data.getCode())
                //角色名称
                .addFn(!Strings.isNullOrEmpty(data.getName()), qEntity.name, () -> {
                    //检查角色名称是否已存在
                    if (jpaRole.exists(qEntity.name.eq(data.getName()).and(qEntity.id.ne(id)))) {
                        return null;
                    }
                    return data.getName();
                })
                //角色简称
                .addFn(!Strings.isNullOrEmpty(data.getAbbr()), qEntity.abbr, () -> {
                    //检查角色简称是否已存在
                    if (jpaRole.exists(qEntity.abbr.eq(data.getAbbr()).and(qEntity.id.ne(id)))) {
                        return null;
                    }
                    return data.getAbbr();
                })
                //角色备注
                .add(!Strings.isNullOrEmpty(data.getRemark()), qEntity.remark, data.getRemark())
                //数据权限范围
                .add(Objects.nonNull(data.getScope()), qEntity.scope, data.getScope())
                //更新数据
                .execute(qEntity.id.eq(id));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean delByIds(@Nonnull final Long[] ids) {
        if (ids.length > 0) {
            final QRoleEntity qEntity = QRoleEntity.roleEntity;
            return queryFactory.delete(qEntity)
                    .where(qEntity.id.in(ids))
                    .execute() > 0;
        }
        return false;
    }
}
