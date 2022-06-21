package top.zenyoung.framework.system.dao.repository.impl;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.google.common.base.Strings;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import top.zenyoung.common.model.Status;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.data.repository.impl.BaseRepositoryImpl;
import top.zenyoung.framework.system.Constants;
import top.zenyoung.framework.system.dao.entity.ConfigEntity;
import top.zenyoung.framework.system.dao.entity.QConfigEntity;
import top.zenyoung.framework.system.dao.entity.QDeptEntity;
import top.zenyoung.framework.system.dao.jpa.JpaConfig;
import top.zenyoung.framework.system.dao.repository.ConfigRepository;
import top.zenyoung.framework.system.dto.ConfigAddDTO;
import top.zenyoung.framework.system.dto.ConfigDTO;
import top.zenyoung.framework.system.dto.ConfigModifyDTO;
import top.zenyoung.framework.system.dto.ConfigQueryDTO;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 参数管理-数据操作接口实现
 *
 * @author young
 */
@Repository
@RequiredArgsConstructor
public class ConfigRepositoryImpl extends BaseRepositoryImpl implements ConfigRepository, Constants {
    private static final String CACHE_KEY = CACHE_PREFIX + "config";
    private final JpaConfig jpaConfig;

    @Override
    @Transactional(readOnly = true, rollbackFor = Throwable.class)
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
        }), jpaConfig, entity -> mapping(entity, ConfigDTO.class));
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Cached(area = CACHE_AREA, name = CACHE_KEY, key = "#id", cacheType = CacheType.BOTH, expire = CACHE_EXPIRE)
    public ConfigDTO getById(@Nonnull final Long id) {
        final AtomicReference<ConfigDTO> ref = new AtomicReference<>(null);
        jpaConfig.findById(id)
                .ifPresent(entity -> {
                    final ConfigDTO data = mapping(entity, ConfigDTO.class);
                    ref.set(data);
                });
        return ref.get();
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Cached(area = CACHE_AREA, name = CACHE_KEY, key = "#key", cacheType = CacheType.BOTH, expire = CACHE_EXPIRE)
    public ConfigDTO getByKey(@Nonnull final String key) {
        final QConfigEntity qConfigEntity = QConfigEntity.configEntity;
        return mapping(queryFactory.selectFrom(qConfigEntity)
                .where(qConfigEntity.key.eq(key))
                .fetchFirst(), ConfigDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Long add(@Nonnull final ConfigAddDTO add) {
        final ConfigEntity entity = mapping(add, ConfigEntity.class);
        entity.setStatus(Status.Enable);
        return jpaConfig.save(entity).getId();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    @CacheInvalidate(area = CACHE_AREA, name = CACHE_KEY, key = "#id")
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

    @Override
    @Transactional(rollbackFor = Throwable.class)
    @CacheInvalidate(area = CACHE_AREA, name = CACHE_KEY, key = "#ids", multi = true)
    public boolean delByIds(@Nonnull final Long[] ids) {
        if (ids.length > 0) {
            final QDeptEntity qDeptEntity = QDeptEntity.deptEntity;
            return queryFactory.delete(qDeptEntity)
                    .where(qDeptEntity.id.in(ids))
                    .execute() > 0;
        }
        return false;
    }
}
