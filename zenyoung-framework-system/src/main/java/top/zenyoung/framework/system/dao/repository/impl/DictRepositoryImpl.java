package top.zenyoung.framework.system.dao.repository.impl;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.data.repository.impl.BaseRepositoryImpl;
import top.zenyoung.framework.exception.ServiceException;
import top.zenyoung.framework.system.Constants;
import top.zenyoung.framework.system.dao.entity.DictDataEntity;
import top.zenyoung.framework.system.dao.entity.DictTypeEntity;
import top.zenyoung.framework.system.dao.entity.QDictDataEntity;
import top.zenyoung.framework.system.dao.entity.QDictTypeEntity;
import top.zenyoung.framework.system.dao.jpa.JpaDictData;
import top.zenyoung.framework.system.dao.jpa.JpaDictType;
import top.zenyoung.framework.system.dao.repository.DictRepository;
import top.zenyoung.framework.system.dto.*;
import top.zenyoung.service.BeanMappingService;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 字典-数据服务接口
 *
 * @author young
 */
@Repository
@RequiredArgsConstructor
public class DictRepositoryImpl extends BaseRepositoryImpl implements DictRepository, Constants {
    private static final String CACHE_DICT_TYPE = CACHE_PREFIX + "-dict-type";
    private static final String CACHE_DICT_DATA = CACHE_PREFIX + "-dict-data";
    private final JPAQueryFactory queryFactory;
    private final JpaDictType jpaDictType;
    private final JpaDictData jpaDictData;

    private final BeanMappingService mappingService;

    @Override
    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    public PagingResult<DictTypeDTO> queryTypes(@Nonnull final DictTypeQueryDTO query) {
        return buildPagingQuery(query, q -> buildDslWhere(new LinkedList<BooleanExpression>() {
            {
                final QDictTypeEntity qEntity = QDictTypeEntity.dictTypeEntity;
                //状态
                if (query.getStatus() != null) {
                    add(qEntity.status.eq(query.getStatus()));
                }
                //字典名称/类型
                final String name;
                if (!Strings.isNullOrEmpty(name = query.getName())) {
                    final String like = "%" + name + "%";
                    add(qEntity.name.like(like).or(qEntity.type.like(like)));
                }
            }
        }), jpaDictType, entity -> mappingService.mapping(entity, DictTypeDTO.class));
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Cached(area = CACHE_AREA, name = CACHE_DICT_TYPE, key = "#typeId", cacheType = CacheType.BOTH, expire = CACHE_EXPIRE)
    public DictTypeDTO getTypeById(@Nonnull final Long typeId) {
        return mappingService.mapping(jpaDictType.getOne(typeId), DictTypeDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Long addType(@Nonnull final DictTypeAddDTO data) {
        final QDictTypeEntity qDictTypeEntity = QDictTypeEntity.dictTypeEntity;
        //检查字典类型
        if (!Strings.isNullOrEmpty(data.getType()) && jpaDictType.exists(qDictTypeEntity.type.eq(data.getType()))) {
            throw new ServiceException("[" + data.getType() + "]已存在");
        }
        final DictTypeEntity entity = mappingService.mapping(data, DictTypeEntity.class);
        //保存数据
        return jpaDictType.save(entity).getId();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    @CacheInvalidate(area = CACHE_AREA, name = CACHE_DICT_TYPE, key = "#typeId")
    public boolean updateType(@Nonnull final Long typeId, @Nonnull final DictTypeModifyDTO data) {
        final QDictTypeEntity qDictTypeEntity = QDictTypeEntity.dictTypeEntity;
        return buildDslUpdateClause(queryFactory.update(qDictTypeEntity))
                //字典名称
                .add(!Strings.isNullOrEmpty(data.getName()), qDictTypeEntity.name, data.getName())
                //字典类型
                .add(!Strings.isNullOrEmpty(data.getType()) && !jpaDictType.exists(qDictTypeEntity.type.eq(data.getType())), qDictTypeEntity.type, data.getType())
                //字典备注
                .add(!Strings.isNullOrEmpty(data.getRemark()), qDictTypeEntity.remark, data.getRemark())
                //状态
                .add(data.getStatus() != null, qDictTypeEntity.status, data.getStatus())
                //更新数据
                .execute(qDictTypeEntity.id.eq(typeId));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    @CacheInvalidate(area = CACHE_AREA, name = CACHE_DICT_TYPE, key = "#typeIds", multi = true)
    public boolean delTypeByIds(@Nonnull final Long[] typeIds) {
        if (typeIds.length > 0) {
            //获取字典类型
            final QDictTypeEntity qDictTypeEntity = QDictTypeEntity.dictTypeEntity;
            final List<String> types = queryFactory.select(qDictTypeEntity.type)
                    .from(qDictTypeEntity).where(qDictTypeEntity.id.in(typeIds)).fetch();
            if (CollectionUtils.isEmpty(types)) {
                return false;
            }
            final QDictDataEntity qDictDataEntity = QDictDataEntity.dictDataEntity;
            //检查删除字典数据
            if (jpaDictData.exists(qDictDataEntity.type.in(types))) {
                throw new ServiceException("有字典数据不允许删除");
            }
            //删除字典类型
            return queryFactory.delete(qDictTypeEntity)
                    .where(qDictTypeEntity.id.in(typeIds))
                    .execute() > 0;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Cached(area = CACHE_AREA, name = CACHE_DICT_DATA, key = "#dictType", cacheType = CacheType.BOTH, expire = CACHE_EXPIRE)
    public List<DictDataDTO> getDataByType(@Nonnull final String dictType) {
        if (Strings.isNullOrEmpty(dictType)) {
            return Lists.newArrayList();
        }
        final QDictDataEntity qDictDataEntity = QDictDataEntity.dictDataEntity;
        return queryFactory.selectFrom(qDictDataEntity)
                .where(qDictDataEntity.type.eq(dictType))
                .fetch()
                .stream()
                .map(entity -> mappingService.mapping(entity, DictDataDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    @CacheInvalidate(area = CACHE_AREA, name = CACHE_DICT_DATA, key = "#typeId")
    public boolean batchAddDatas(@Nonnull final Long typeId, @Nonnull final List<DictDataAddDTO> items) {
        if (typeId > 0 && !CollectionUtils.isEmpty(items)) {
            //字典类型
            final QDictTypeEntity qDictTypeEntity = QDictTypeEntity.dictTypeEntity;
            final String type = queryFactory.select(qDictTypeEntity.type).from(qDictTypeEntity)
                    .where(qDictTypeEntity.id.eq(typeId)).fetchFirst();
            if (!Strings.isNullOrEmpty(type)) {
                final AtomicInteger ref = new AtomicInteger(getDataMaxByType(type));
                jpaDictData.saveAll(items.stream()
                        .map(item -> {
                            final DictDataEntity entity = mappingService.mapping(item, DictDataEntity.class);
                            //字典代码
                            if (entity.getCode() == null || entity.getCode() <= 0) {
                                entity.setCode(ref.incrementAndGet());
                            }
                            return entity;
                        })
                        .collect(Collectors.toList())
                );
            }
        }
        return false;
    }

    private Integer getDataMaxByType(@Nonnull final String type) {
        if (!Strings.isNullOrEmpty(type)) {
            final QDictDataEntity qDictDataEntity = QDictDataEntity.dictDataEntity;
            final Integer max = queryFactory.select(qDictDataEntity.code.max())
                    .from(qDictDataEntity)
                    .where(qDictDataEntity.type.eq(type))
                    .fetchFirst();
            if (max != null) {
                return max;
            }
        }
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    @CacheInvalidate(area = CACHE_AREA, name = CACHE_DICT_DATA, key = "#dataId")
    public boolean updateData(@Nonnull final Long dataId, @Nonnull final DictDataModifyDTO data) {
        final QDictDataEntity qDictDataEntity = QDictDataEntity.dictDataEntity;
        return buildDslUpdateClause(queryFactory.update(qDictDataEntity))
                //字典代码
                .add(data.getCode() != null, qDictDataEntity.code, data.getCode())
                //字典标签
                .add(!Strings.isNullOrEmpty(data.getLabel()), qDictDataEntity.label, data.getLabel())
                //字典键值
                .add(!Strings.isNullOrEmpty(data.getValue()), qDictDataEntity.value, data.getValue())
                //是否默认
                .add(data.getIsDefault() != null, qDictDataEntity.isDefault, data.getIsDefault())
                //样式属性
                .add(!Strings.isNullOrEmpty(data.getCssClass()), qDictDataEntity.cssClass, data.getCssClass())
                //表格回显样式
                .add(!Strings.isNullOrEmpty(data.getListClass()), qDictDataEntity.listClass, data.getListClass())
                //备注
                .add(!Strings.isNullOrEmpty(data.getRemark()), qDictDataEntity.remark, data.getRemark())
                //状态
                .add(data.getStatus() != null, qDictDataEntity.status, data.getStatus())
                .execute(qDictDataEntity.id.eq(dataId));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    @CacheInvalidate(area = CACHE_AREA, name = CACHE_DICT_DATA, key = "#dataIds", multi = true)
    public boolean delDataByIds(@Nonnull final Long[] dataIds) {
        if (dataIds.length > 0) {
            final QDictDataEntity qDictDataEntity = QDictDataEntity.dictDataEntity;
            return queryFactory.delete(qDictDataEntity)
                    .where(qDictDataEntity.id.in(dataIds))
                    .execute() > 0;
        }
        return false;
    }
}
