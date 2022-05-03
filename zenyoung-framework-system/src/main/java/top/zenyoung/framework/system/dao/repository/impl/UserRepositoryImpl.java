package top.zenyoung.framework.system.dao.repository.impl;

import com.google.common.base.Strings;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.data.repository.impl.BaseRepositoryImpl;
import top.zenyoung.framework.auth.AuthUser;
import top.zenyoung.framework.system.dao.entity.*;
import top.zenyoung.framework.system.dao.jpa.JpaPost;
import top.zenyoung.framework.system.dao.jpa.JpaRole;
import top.zenyoung.framework.system.dao.jpa.JpaUser;
import top.zenyoung.framework.system.dao.repository.DeptRepository;
import top.zenyoung.framework.system.dao.repository.UserRepository;
import top.zenyoung.framework.system.dto.*;
import top.zenyoung.service.BeanMappingService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 用户-数据服务接口实现
 *
 * @author young
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl extends BaseRepositoryImpl implements UserRepository {
    private final JPAQueryFactory queryFactory;
    private final BeanMappingService mappingService;

    private final JpaUser jpaUser;
    private final JpaPost jpaPost;
    private final JpaRole jpaRole;
    private final DeptRepository deptRepository;

    private final PasswordEncoder pwdEncoder;

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
            //所属部门
            final Long deptId;
            if ((deptId = entity.getDeptId()) != null && deptId > 0) {
                data.setDept(deptRepository.getDeptInfoById(deptId));
            }
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

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Long add(@Nonnull final UserAddDTO data) {
        final UserEntity entity = mappingService.mapping(data, UserEntity.class);
        //主键ID
        entity.setId(sequence.nextId());
        //密码
        if (!Strings.isNullOrEmpty(data.getPasswd())) {
            entity.setPasswd(pwdEncoder.encode(data.getPasswd()));
        }
        //所属岗位集合
        updatePosts(entity, data.getPosts());
        //所属角色集合
        updateRoles(entity, data.getRoles());
        //保存数据
        return jpaUser.save(entity).getId();
    }

    private void updatePosts(@Nonnull final UserEntity entity, @Nullable final List<Long> posts) {
        if (!CollectionUtils.isEmpty(posts)) {
            final QPostEntity qEntity = QPostEntity.postEntity;
            entity.setPosts(
                    StreamSupport.stream(jpaPost.findAll(qEntity.id.in(posts)).spliterator(), false)
                            .collect(Collectors.toList())
            );
        }
    }

    private void updateRoles(@Nonnull final UserEntity entity, @Nullable final List<Long> roles) {
        if (!CollectionUtils.isEmpty(roles)) {
            final QRoleEntity qEntity = QRoleEntity.roleEntity;
            entity.setRoles(
                    StreamSupport.stream(jpaRole.findAll(qEntity.id.in(roles)).spliterator(), false)
                            .collect(Collectors.toList())
            );
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean update(@Nonnull final Long id, @Nonnull final UserModifyDTO data) {
        final QUserEntity qUserEntity = QUserEntity.userEntity;
        boolean ret = buildDslUpdateClause(queryFactory.update(qUserEntity))
                //用户姓名
                .add(!Strings.isNullOrEmpty(data.getName()), qUserEntity.name, data.getName())
                //用户账号
                .addFn(!Strings.isNullOrEmpty(data.getAccount()), qUserEntity.account, () -> {
                    //检查账号是否已存在
                    if (checkAccountCount(data.getAccount()) > 1) {
                        return null;
                    }
                    return data.getAccount();
                })
                //联系电话
                .add(!Strings.isNullOrEmpty(data.getMobile()), qUserEntity.mobile, data.getMobile())
                //邮箱
                .add(!Strings.isNullOrEmpty(data.getEmail()), qUserEntity.email, data.getEmail())
                //状态
                .add(data.getStatus() != null, qUserEntity.status, data.getStatus())
                //密码
                .addFn(!Strings.isNullOrEmpty(data.getPasswd()), qUserEntity.passwd, () -> pwdEncoder.encode(data.getPasswd()))
                //所属部门ID
                .add(data.getDeptId() != null && data.getDeptId() > 0, qUserEntity.deptId, data.getDeptId())
                .execute(qUserEntity.id.eq(id));
        if (ret) {
            //所属岗位集合/角色集合是否存在
            if (CollectionUtils.isEmpty(data.getPosts()) || CollectionUtils.isEmpty(data.getRoles())) {
                jpaUser.findById(id)
                        .ifPresent(entity -> {
                            //所属岗位集合
                            updatePosts(entity, data.getPosts());
                            //所属角色集合
                            updateRoles(entity, data.getRoles());
                        });
            }
        }
        return ret;
    }

    private int checkAccountCount(@Nullable final String account) {
        if (!Strings.isNullOrEmpty(account)) {
            final QUserEntity qEntity = QUserEntity.userEntity;
            return (int) jpaUser.count(qEntity.account.eq(account));
        }
        return 0;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean delByIds(@Nonnull final Long[] ids) {
        if (ids.length > 0) {
            final QUserEntity qEntity = QUserEntity.userEntity;
            return queryFactory.delete(qEntity)
                    .where(qEntity.id.in(ids))
                    .execute() > 0;
        }
        return false;
    }

    @Transactional(readOnly = true)
    @Override
    public AuthUser findByAccount(@Nonnull final String account) {
        Assert.hasText(account, "'account'不能为空");
        final QUserEntity qUserEntity = QUserEntity.userEntity;
        final UserEntity entity = queryFactory.selectFrom(qUserEntity)
                .where(qUserEntity.account.eq(account))
                .fetchFirst();
        if (entity != null) {
            return AuthUser.builder()
                    .id(entity.getId())
                    .account(entity.getAccount())
                    .name(entity.getName())
                    .password(entity.getPasswd())
                    .nick(entity.getName())
                    .roles(entity.getRoles().stream().map(RoleEntity::getAbbr).collect(Collectors.toList()))
                    .status(entity.getStatus())
                    .build();
        }
        return null;
    }
}
