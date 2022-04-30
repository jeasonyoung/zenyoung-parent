package top.zenyoung.framework.system.dao.repository.impl;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.model.Status;
import top.zenyoung.data.querydsl.DslUpdateClause;
import top.zenyoung.data.repository.impl.BaseRepositoryImpl;
import top.zenyoung.framework.system.dao.entity.DeptEntity;
import top.zenyoung.framework.system.dao.entity.QDeptEntity;
import top.zenyoung.framework.system.dao.jpa.JpaDept;
import top.zenyoung.framework.system.dao.repository.DeptRepository;
import top.zenyoung.framework.system.dto.DeptAddDTO;
import top.zenyoung.framework.system.dto.DeptDTO;
import top.zenyoung.framework.system.dto.DeptInfoDTO;
import top.zenyoung.framework.system.dto.DeptModifyDTO;
import top.zenyoung.service.BeanMappingService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 部门-数据操作接口实现
 *
 * @author young
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DeptRepositoryImpl extends BaseRepositoryImpl implements DeptRepository {
    private static final String DEPT_ANCESTOR_SEP = ",";
    private final JPAQueryFactory queryFactory;
    private final JpaDept jpaDept;
    private final BeanMappingService mappingService;

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public List<DeptDTO> getDeptWithChildren(@Nullable final Long parentDeptId) {
        final QDeptEntity qEntity = QDeptEntity.deptEntity;
        final JPAQuery<DeptEntity> query = queryFactory.selectFrom(qEntity);
        if (parentDeptId != null && parentDeptId > 0) {
            query.where(Expressions.booleanTemplate("find_in_set({0}, ancestors) > 0", parentDeptId));
        }
        return query.fetch().stream()
                .map(this::buildConvert)
                .sorted(Comparator.comparingLong(item -> {
                    if (item.getParentId() == null || item.getParentId() <= 0) {
                        return item.getCode();
                    }
                    return item.getParentId() + item.getCode();
                }))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public DeptDTO getDept(@Nonnull final Long id) {
        if (id > 0) {
            return buildConvert(jpaDept.getById(id));
        }
        return null;
    }

    private DeptDTO buildConvert(@Nullable final DeptEntity entity) {
        if (entity != null) {
            final DeptDTO data = new DeptDTO();
            BeanUtils.copyProperties(entity, data, "roles");
            return data;
        }
        return null;
    }

    @Override
    public DeptInfoDTO getDeptInfoById(@Nonnull final Long id) {
        if (id > 0) {
            final QDeptEntity qDeptEntity = QDeptEntity.deptEntity;
            final Tuple tuple = queryFactory.from(qDeptEntity)
                    .select(qDeptEntity.id, qDeptEntity.name)
                    .fetchOne();
            if (tuple != null) {
                return DeptInfoDTO.of(tuple.get(qDeptEntity.id), tuple.get(qDeptEntity.name));
            }
        }
        return null;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Long addDept(@Nonnull final DeptAddDTO data) {
        log.debug("addDept(data: {})...", data);
        final DeptEntity entity = mappingService.mapping(data, DeptEntity.class);
        //主键ID
        entity.setId(sequence.nextId());
        //状态
        entity.setStatus(Status.Enable);
        //排序号处理
        if (entity.getCode() != null) {
            entity.setCode(getMaxCode(data.getParentId()) + 1);
        }
        //检查父节点
        if (data.getParentId() != null && data.getParentId() > 0) {
            final DeptEntity parent = jpaDept.getById(data.getParentId());
            if (Status.Enable != parent.getStatus()) {
                throw new RuntimeException("部门停用,不允许新增");
            }
            entity.setAncestors(parent.getAncestors() + DEPT_ANCESTOR_SEP + parent.getParentId());
        }
        //保存数据
        return jpaDept.save(entity).getId();
    }

    private Integer getMaxCode(@Nullable final Long parentDeptId) {
        final QDeptEntity qEntity = QDeptEntity.deptEntity;
        final JPAQuery<Integer> query = queryFactory.select(qEntity.code.max()).from(qEntity);
        if (parentDeptId != null && parentDeptId >= 0) {
            query.where(qEntity.parentId.eq(parentDeptId));
        }
        final Integer max = query.fetchOne();
        return max == null ? 0 : max;
    }

    @Override
    public boolean modifyDept(@Nonnull final Long id, @Nonnull final DeptModifyDTO data) {
        Assert.isTrue(id > 0, "'data.id'不能为空!");
        final QDeptEntity qDeptEntity = QDeptEntity.deptEntity;
        final AtomicReference<String> refNewAncestors = new AtomicReference<>(null);
        final DslUpdateClause updateClause = buildDslUpdateClause(queryFactory.update(qDeptEntity))
                //部门代码
                .add(data.getCode() != null, qDeptEntity.code, data.getCode())
                //部门名称
                .add(!Strings.isNullOrEmpty(data.getName()), qDeptEntity.name, data.getName())
                //负责人
                .add(!Strings.isNullOrEmpty(data.getLeader()), qDeptEntity.leader, data.getLeader())
                //联系电话
                .add(!Strings.isNullOrEmpty(data.getMobile()), qDeptEntity.mobile, data.getMobile())
                //邮箱
                .add(!Strings.isNullOrEmpty(data.getEmail()), qDeptEntity.email, data.getEmail())
                //状态
                .add(data.getStatus() != null, qDeptEntity.status, data.getStatus())
                //上级部门ID
                .addFn(data.getParentId() != null, qDeptEntity.parentId, () -> {
                    final String ancestors = getAncestors(data.getParentId());
                    if (!Strings.isNullOrEmpty(ancestors)) {
                        final String newAncestors = ancestors + DEPT_ANCESTOR_SEP + data.getParentId();
                        refNewAncestors.set(newAncestors);
                        return data.getParentId();
                    }
                    return null;
                });
        final String newAncestors = refNewAncestors.get();
        if (!Strings.isNullOrEmpty(newAncestors)) {
            updateClause.add(qDeptEntity.ancestors, newAncestors);
        }
        //更新数据
        final boolean ret = updateClause.execute(qDeptEntity.id.eq(id));
        if (ret) {
            final String oldAncestors = getAncestors(id);
            if (!Strings.isNullOrEmpty(oldAncestors) && !Strings.isNullOrEmpty(newAncestors)) {
                updateChildrenAncestors(id, newAncestors, oldAncestors);
            }
            //如果该部门是启用状态，则启用该部门的所有上级部门
            if (data.getStatus() == Status.Enable && !Strings.isNullOrEmpty(oldAncestors)) {
                updateParentStatusEnable(oldAncestors);
            }
        }
        return ret;
    }

    private String getAncestors(@Nullable final Long deptId) {
        if (Objects.nonNull(deptId) && deptId > 0) {
            final QDeptEntity qDeptEntity = QDeptEntity.deptEntity;
            return queryFactory.from(qDeptEntity)
                    .select(qDeptEntity.ancestors)
                    .where(qDeptEntity.id.eq(deptId))
                    .fetchOne();
        }
        return null;
    }

    private void updateChildrenAncestors(@Nonnull final Long deptId, @Nonnull final String newAncestors, @Nonnull final String oldAncestors) {
        final QDeptEntity qDept = QDeptEntity.deptEntity;
        queryFactory.select(qDept.id, qDept.ancestors)
                .from(qDept)
                .where(Expressions.booleanTemplate("find_in_set({0}, ancestors) > 0", deptId))
                .fetch()
                .parallelStream()
                .forEach(tuple -> {
                    final Long id = tuple.get(qDept.id);
                    final String ancestors = tuple.get(qDept.ancestors);
                    if (id != null && id > 0 && !Strings.isNullOrEmpty(ancestors)) {
                        final String ret = ancestors.replaceFirst(oldAncestors, newAncestors);
                        queryFactory.update(qDept)
                                .set(qDept.ancestors, ret)
                                .where(qDept.id.eq(id))
                                .execute();
                    }
                });
    }

    private void updateParentStatusEnable(@Nonnull final String ancestors) {
        if (!Strings.isNullOrEmpty(ancestors)) {
            final QDeptEntity qDept = QDeptEntity.deptEntity;
            final Status val = Status.Enable;
            Splitter.on(DEPT_ANCESTOR_SEP).omitEmptyStrings().trimResults().split(ancestors).forEach(deptId -> {
                try {
                    final Long id = Long.parseLong(deptId);
                    queryFactory.update(qDept)
                            .set(qDept.status, val)
                            .where(qDept.id.eq(id));
                } catch (Throwable ex) {
                    log.warn("updateParentStatusEnable(ancestors: {})-exp: {}", ancestors, ex.getMessage());
                }
            });
        }
    }

    @Override
    public int delDeptByIds(@Nonnull final List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            final QDeptEntity qDept = QDeptEntity.deptEntity;
            return (int) queryFactory.delete(qDept)
                    .where(qDept.id.in(ids))
                    .execute();
        }
        return 0;
    }
}
