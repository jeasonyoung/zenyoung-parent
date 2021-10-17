package ${packageName}.dao.repository.impl;

import ${packageName}.dao.dto.${className}DTO;
import ${packageName}.dao.jpa.${className}Jpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.querydsl.jpa.impl.JPAQueryFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.Assert;

import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.data.repository.impl.BaseRepositoryImpl;

import javax.annotation.Nonnull;

/**
 * ${comment!}-数据服务接口实现
 * <#assign lastTime = .now>
 * @author ${author!}
 * @version 1.0
 * @date ${lastTime?iso_utc}
 **/
@Slf4j
@Repository
@RequiredArgsConstructor
public class ${className}RepositoryImpl extends BaseRepositoryImpl implements ${className}Repository {
    private final JPAQueryFactory queryFactory;
    private final ${className}Jpa jpa${className};

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public PagingResult<${className}DTO> query(@Nonnull final PagingQuery<${className}DTO> pagingQuery) {
        return buildPagingQuery(pagingQuery, query -> {
            final Q${className}Entity qEntity = Q${className}Entity.${lowerClassName}Entity;
            return buildDslWhere(new LinkedList<>() {
                {
                    ///TODO::查询条件处理
                }
            });
        }, jpa${className}, entity->{
            final ${className}DTO data = new ${className}DTO();
            BeanUtils.copyProperties(entity, data);
            return data;
        });
    }

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public ${className}DTO loadById(@Nonnull final Long id) {
        Assert.isTrue(Id > 0, "'Id'不能为空!");
        final AtomicReference<${className}DTO> ref = new AtomicReference<>(null);
        jpa${className}.findById(packageId)
                .ifPresent(entity -> {
                    final ${className}DTO data = new ${className}DTO();
                    BeanUtils.copyProperties(entity, data);
                    ref.set(data);
                });
        return ref.get();
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Long add(@Nonnull final ${className}DTO data) {
        final ${className}Entity entity = new ${className}Entity();
        BeanUtils.copyProperties(data, entity);
        return jpa${className}.save(entity).getId();
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public void modify(@Nonnull final ${className}DTO data) {
        final Q${className}Entity qEntity = Q${className}Entity.${lowerClassName}Entity;
        final JPAUpdateClause updateClause = queryFactory.update(qEntity);
        final boolean isUpdate = buildDslUpdateClause(updateClause, new LinkedHashMap<>() {
            {
                ///TODO::字段更新处理
            }
        });
        if (isUpdate) {
            final long ret = updateClause.where(qEntity.id.eq(modify.getId())).execute();
            log.info("modify(modify: {})=> {}", data, ret);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public void delById(@Nonnull final Long id) {
        Assert.isTrue(id > 0, "'id' > 0");
        jpa${className}.deleteById(id);
    }
}