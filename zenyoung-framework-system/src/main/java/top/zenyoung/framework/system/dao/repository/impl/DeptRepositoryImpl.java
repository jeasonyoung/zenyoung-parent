package top.zenyoung.framework.system.dao.repository.impl;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
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
import top.zenyoung.common.sequence.Sequence;
import top.zenyoung.data.repository.impl.BaseRepositoryImpl;
import top.zenyoung.framework.system.dao.dto.DeptAddDTO;
import top.zenyoung.framework.system.dao.dto.DeptLoadDTO;
import top.zenyoung.framework.system.dao.dto.DeptModifyDTO;
import top.zenyoung.framework.system.dao.entity.DeptEntity;
import top.zenyoung.framework.system.dao.entity.QDeptEntity;
import top.zenyoung.framework.system.dao.jpa.JpaDept;
import top.zenyoung.framework.system.dao.repository.DeptRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
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

    private final Sequence<Long> snowFlake;

    @Transactional(readOnly = true)
    @Override
    public List<DeptLoadDTO> getAllDepts() {
        return jpaDept.findAll().stream()
                .map(this::buildConvert)
                .sorted(Comparator.comparingLong(item -> {
                    if (item.getParentId() == null || item.getParentId() <= 0) {
                        return item.getCode();
                    }
                    return item.getParentId() + item.getCode();
                }))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public DeptLoadDTO getDept(@Nonnull final Long id) {
        if (id > 0) {
            return buildConvert(jpaDept.getOne(id));
        }
        return null;
    }

    private DeptLoadDTO buildConvert(@Nullable final DeptEntity entity) {
        if (entity != null) {
            final DeptLoadDTO data = new DeptLoadDTO();
            BeanUtils.copyProperties(entity, data, "roles");
            return data;
        }
        return null;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Long addDept(@Nonnull final DeptAddDTO data) {
        log.debug("addDept(data: {})...", data);
        Assert.hasText(data.getName(), "'data.name'不能为空!");
        final DeptEntity entity = new DeptEntity();
        BeanUtils.copyProperties(data, entity);
        entity.setId(snowFlake.nextId());
        //排序号处理
        if (entity.getCode() != null) {
            entity.setCode(getMaxCode(data.getParentId()) + 1);
        }
        //检查父节点
        if (data.getParentId() != null && data.getParentId() > 0) {
            final DeptEntity parent = jpaDept.getOne(data.getParentId());
            if (Status.Enable != parent.getStatus()) {
                throw new RuntimeException("部门停用,不允许新增");
            }
            entity.setAncestors(parent.getAncestors() + DEPT_ANCESTOR_SEP + parent.getParentId());
        }
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
    public void modifyDept(@Nonnull final DeptModifyDTO data) {
        Assert.isTrue(data.getId() != null && data.getId() > 0, "'data.id'不能为空!");
        final DeptEntity newParent = data.getParentId() != null && data.getParentId() >= 0 ? jpaDept.getOne(data.getParentId()) : null;
        final DeptEntity entity = jpaDept.getOne(data.getId());
        BeanUtils.copyProperties(data, entity);
        if (newParent != null) {
            final String newAncestors = newParent.getAncestors() + DEPT_ANCESTOR_SEP + newParent.getId();
            final String oldAncestors = entity.getAncestors();
            entity.setAncestors(newAncestors);
            updateChildrenAncestors(entity.getId(), newAncestors, oldAncestors);
        }
        if (data.getStatus() == Status.Enable && !Strings.isNullOrEmpty(entity.getAncestors())) {
            //如果该部门是启用状态，则启用该部门的所有上级部门
            updateParentStatusEnable(entity.getAncestors());
        }
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
