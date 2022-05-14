package top.zenyoung.framework.system.dao.repository.impl;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.common.util.CacheUtils;
import top.zenyoung.data.repository.impl.BaseRepositoryImpl;
import top.zenyoung.framework.system.dao.entity.PostEntity;
import top.zenyoung.framework.system.dao.entity.QPostEntity;
import top.zenyoung.framework.system.dao.jpa.JpaPost;
import top.zenyoung.framework.system.dao.repository.DeptRepository;
import top.zenyoung.framework.system.dao.repository.PostRepository;
import top.zenyoung.framework.system.dto.*;
import top.zenyoung.service.BeanMappingService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.LinkedList;

/**
 * 岗位-数据服务接口实现
 *
 * @author young
 */
@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl extends BaseRepositoryImpl implements PostRepository {
    private static final Cache<Long, DeptInfoDTO> DEPT_CACHE = CacheUtils.createCache(50, Duration.ofMinutes(30));
    private final JPAQueryFactory queryFactory;
    private final JpaPost jpaPost;
    private final DeptRepository deptRepository;
    private final BeanMappingService mappingService;

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public PagingResult<PostDTO> query(@Nonnull final PostQueryDTO query) {
        return buildPagingQuery(query, q -> buildDslWhere(new LinkedList<BooleanExpression>() {
            {
                final QPostEntity qEntity = QPostEntity.postEntity;
                //所属部门ID
                if (query.getDeptId() != null && query.getDeptId() > 0) {
                    add(qEntity.deptId.eq(query.getDeptId()));
                }
                //状态
                if (query.getStatus() != null) {
                    add(qEntity.status.eq(query.getStatus()));
                }
                //岗位编码/岗位名称
                final String name;
                if (!Strings.isNullOrEmpty(name = query.getName())) {
                    final String like = "%" + name + "%";
                    add(qEntity.name.like(like).or(qEntity.code.like(like)));
                }
            }
        }), jpaPost, this::convert);
    }

    private PostDTO convert(@Nullable final PostEntity entity) {
        if (entity == null) {
            return null;
        }
        final PostDTO data = mappingService.mapping(entity, PostDTO.class);
        final Long deptId;
        if ((deptId = entity.getDeptId()) != null && deptId > 0) {
            data.setDept(CacheUtils.getCacheValue(DEPT_CACHE, deptId, () -> deptRepository.getDeptInfoById(deptId)));
        }
        return data;
    }

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public PostDTO getById(@Nonnull final Long id) {
        return convert(jpaPost.getById(id));
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Long add(@Nonnull final PostAddDTO data) {
        final PostEntity entity = mappingService.mapping(data, PostEntity.class);
        //保存数据
        return jpaPost.save(entity).getId();
    }

    private int checkCodeCount(@Nullable final String code) {
        if (!Strings.isNullOrEmpty(code)) {
            final QPostEntity qPostEntity = QPostEntity.postEntity;
            return (int) jpaPost.count(qPostEntity.code.eq(code));
        }
        return 0;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean update(@Nonnull final Long id, @Nonnull final PostModifyDTO data) {
        final QPostEntity qPostEntity = QPostEntity.postEntity;
        return buildDslUpdateClause(queryFactory.update(qPostEntity))
                //岗位编码
                .addFn(!Strings.isNullOrEmpty(data.getCode()), qPostEntity.code, () -> {
                    if (checkCodeCount(data.getCode()) > 1) {
                        return null;
                    }
                    return data.getCode();
                })
                //岗位名称
                .add(!Strings.isNullOrEmpty(data.getName()), qPostEntity.name, data.getName())
                //状态
                .add(data.getStatus() != null, qPostEntity.status, data.getStatus())
                //所属部门ID
                .add(data.getDeptId() != null && data.getDeptId() > 0, qPostEntity.deptId, data.getDeptId())
                //更新数据
                .execute(qPostEntity.id.eq(id));
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean delByIds(@Nonnull final Long[] ids) {
        if (ids.length > 0) {
            final QPostEntity qPostEntity = QPostEntity.postEntity;
            return queryFactory.delete(qPostEntity)
                    .where(qPostEntity.id.in(ids))
                    .execute() > 0;
        }
        return false;
    }
}
