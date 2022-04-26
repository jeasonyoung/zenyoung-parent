package top.zenyoung.framework.system.dao.repository.impl;

import com.google.common.base.Strings;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.data.repository.impl.BaseRepositoryImpl;
import top.zenyoung.framework.system.dao.entity.QUserEntity;
import top.zenyoung.framework.system.dao.entity.UserEntity;
import top.zenyoung.framework.system.dao.jpa.JpaUser;
import top.zenyoung.framework.system.dao.repository.UserRepository;
import top.zenyoung.framework.system.dto.*;
import top.zenyoung.service.BeanMappingService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * 用户-数据服务接口实现
 *
 * @author young
 */
@Service
@RequiredArgsConstructor
public class UserRepositoryImpl extends BaseRepositoryImpl implements UserRepository {
    private final JPAQueryFactory queryFactory;
    private final BeanMappingService mappingService;

    private final JpaUser jpaUser;

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public PagingResult<UserDTO> query(@Nonnull final UserQueryDTO query) {
        return buildPagingQuery(query, q -> buildDslWhere(new LinkedList<BooleanExpression>() {
            {
                final QUserEntity qEntity = QUserEntity.userEntity;
                //所属部门ID
                if (query.getDeptId() != null && query.getDeptId() > 0) {
                    add(qEntity.deptId.eq(query.getDeptId()));
                }
                //状态
                if (query.getStatus() != null) {
                    add(qEntity.status.eq(query.getStatus()));
                }
                //用户姓名/账号/手机号码
                final String name;
                if (!Strings.isNullOrEmpty(name = query.getName())) {
                    final String like = "%" + name + "%";
                    add(qEntity.name.like(like).or(qEntity.account.like(like)).or(qEntity.mobile.like(like)));
                }
            }
        }), jpaUser, this::convert);
    }

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public UserDTO getById(@Nonnull final Long id) {
        return convert(jpaUser.getById(id));
    }

    private UserDTO convert(@Nullable final UserEntity entity) {
        if (entity != null) {
            final UserDTO data = new UserDTO();
            BeanUtils.copyProperties(entity, data, "posts", "roles");
            //岗位集合
            data.setPosts(entity.getPosts().stream()
                    .map(p -> PostInfoDTO.of(p.getId(), p.getName()))
                    .collect(Collectors.toList())
            );
            //角色集合
            data.setRoles(entity.getRoles().stream()
                    .map(r -> RoleInfoDTO.of(r.getId(), r.getName()))
                    .collect(Collectors.toList())
            );
            return data;
        }
        return null;
    }

    @Override
    public Long add(@Nonnull final UserAddDTO data) {
        return null;
    }

    @Override
    public boolean update(@Nonnull Long id, @Nonnull UserModifyDTO data) {
        return false;
    }

    @Override
    public boolean delByIds(@Nonnull Long[] ids) {
        return false;
    }
}
