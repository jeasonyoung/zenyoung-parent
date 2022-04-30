package top.zenyoung.framework.system.dao.repository.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.zenyoung.common.model.Status;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.data.repository.impl.BaseRepositoryImpl;
import top.zenyoung.framework.system.dao.entity.ConfigEntity;
import top.zenyoung.framework.system.dao.entity.QConfigEntity;
import top.zenyoung.framework.system.dao.jpa.JpaConfig;
import top.zenyoung.framework.system.dao.repository.ConfigRepository;
import top.zenyoung.framework.system.dto.ConfigAddDTO;
import top.zenyoung.framework.system.dto.ConfigDTO;
import top.zenyoung.framework.system.dto.ConfigModifyDTO;
import top.zenyoung.framework.system.dto.ConfigQueryDTO;
import top.zenyoung.service.BeanMappingService;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 参数管理-数据操作接口实现
 *
 * @author young
 */
@Service
@RequiredArgsConstructor
public class ConfigRepositoryImpl extends BaseRepositoryImpl implements ConfigRepository {
    private final JPAQueryFactory queryFactory;
    private final BeanMappingService mappingService;

    private final JpaConfig jpaConfig;

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public PagingResult<ConfigDTO> query(@Nonnull final ConfigQueryDTO query) {
        return buildPagingQuery(query, q -> buildDslWhere(new LinkedList<BooleanExpression>() {
            {
                final QConfigEntity qConfigEntity = QConfigEntity.configEntity;
                //参数名
                if (!Strings.isNullOrEmpty(query.getName())) {
                    add(qConfigEntity.name.like("%" + query.getName() + "%"));
                }
                //状态
                if (query.getStatus() != null) {
                    add(qConfigEntity.status.eq(query.getStatus()));
                }
            }
        }), jpaConfig, entity -> mappingService.mapping(entity, ConfigDTO.class));
    }

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public ConfigDTO getById(@Nonnull final Long id) {
        final AtomicReference<ConfigDTO> ref = new AtomicReference<>(null);
        jpaConfig.findById(id)
                .ifPresent(entity -> {
                    final ConfigDTO data = mappingService.mapping(entity, ConfigDTO.class);
                    ref.set(data);
                });
        return ref.get();
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Long add(@Nonnull final ConfigAddDTO add) {
        final ConfigEntity entity = mappingService.mapping(add, ConfigEntity.class);
        entity.setId(sequence.nextId());
        entity.setStatus(Status.Enable);
        return jpaConfig.save(entity).getId();
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean update(@Nonnull final Long id, @Nonnull final ConfigModifyDTO data) {
        final QConfigEntity qEntity = QConfigEntity.configEntity;
        return buildDslUpdateClause(queryFactory.update(qEntity))
                //参数名称
                .add(!Strings.isNullOrEmpty(data.getName()), qEntity.name, data.getName())
                //参数键值
                .add(!Strings.isNullOrEmpty(data.getVal()), qEntity.val, data.getVal())
                //系统内置(0:是,1:否)
                .add(data.getStatus() != null, qEntity.status, data.getStatus())
                //执行
                .execute(qEntity.id.eq(id));
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean delByIds(@Nonnull final Long[] ids) {
        if (ids.length > 0) {
            jpaConfig.deleteAllById(Lists.newArrayList(ids));
        }
        return false;
    }
}
