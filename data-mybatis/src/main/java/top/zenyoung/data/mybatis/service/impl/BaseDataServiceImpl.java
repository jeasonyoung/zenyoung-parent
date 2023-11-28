package top.zenyoung.data.mybatis.service.impl;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import top.zenyoung.boot.util.SecurityUtils;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.common.mapping.BeanMapping;
import top.zenyoung.common.mapping.BeanMappingDefault;
import top.zenyoung.common.model.Status;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.data.entity.Model;
import top.zenyoung.data.mybatis.entity.ModelFieldHelper;
import top.zenyoung.data.mybatis.enums.PoConstant;
import top.zenyoung.data.mybatis.mapper.ModelMapper;
import top.zenyoung.data.mybatis.service.DataService;
import top.zenyoung.data.mybatis.util.MybatisPlusUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ORM-操作服务接口实现基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseDataServiceImpl<M extends Model<K>, K extends Serializable> implements DataService<M, K>, InitializingBean {
    protected static final int BATCH_SIZE = 500;
    private static final BeanMapping beanMapping = BeanMappingDefault.INSTANCE;
    private final Map<Integer, Class<?>> clsMaps = Maps.newConcurrentMap();
    private final ModelFieldHelper<M> poPoFieldHelper = ModelFieldHelper.of(this.getModelClass());

    @Autowired(required = false)
    private IdSequence idSequence;

    private Class<?> getGenericType(final int index) {
        return clsMaps.computeIfAbsent(index, idx -> ReflectionKit.getSuperClassGenericType(getClass(), BaseDataServiceImpl.class, idx));
    }

    @SuppressWarnings({"unchecked"})
    protected final Class<M> getModelClass() {
        return (Class<M>) getGenericType(0);
    }

    /**
     * 生成主键ID
     *
     * @return 主键ID
     */
    @SuppressWarnings({"unchecked"})
    protected K genId() {
        return Optional.ofNullable(idSequence)
                .map(idSeq -> {
                    final Long id = idSeq.nextId();
                    final Class<K> cls = (Class<K>) getGenericType(1);
                    if (cls == Long.class) {
                        return cls.cast(id);
                    }
                    if (cls == String.class) {
                        return cls.cast(String.valueOf(id));
                    }
                    return cls.cast(id);
                })
                .orElse(null);
    }

    /**
     * 获取Mapper
     *
     * @return Mapper
     */
    @Nonnull
    protected abstract ModelMapper<M, K> getMapper();

    @Override
    public M getById(@Nonnull final K id) {
        return getById(id, false);
    }

    protected M getById(@Nonnull final K id, final boolean logicDel) {
        if (logicDel) {
            return getMapper().selectPhysicalById(id);
        }
        return getMapper().selectById(id);
    }

    @Override
    public void afterPropertiesSet() {
        poPoFieldHelper.init();
    }

    @Override
    public M getOne(@Nonnull final Consumer<LambdaQueryWrapper<M>> consumer) {
        final LambdaQueryWrapper<M> queryWrapper = Wrappers.lambdaQuery(getModelClass());
        consumer.accept(queryWrapper);
        return getOne(queryWrapper);
    }

    @Override
    public M getOne(@Nonnull final Wrapper<M> query) {
        final List<M> pos = this.queryList(query);
        return CollectionUtils.isEmpty(pos) ? null : pos.get(0);
    }

    @Override
    public int count(@Nonnull final Consumer<LambdaQueryWrapper<M>> consumer) {
        final LambdaQueryWrapper<M> queryWrapper = Wrappers.lambdaQuery(getModelClass());
        consumer.accept(queryWrapper);
        return count(queryWrapper);
    }

    @Override
    public int count(@Nonnull final Wrapper<M> query) {
        return (int) SqlHelper.retCount(getMapper().selectCount(query));
    }

    @Override
    public List<M> queryList(@Nonnull final Consumer<LambdaQueryWrapper<M>> consumer) {
        final LambdaQueryWrapper<M> queryWrapper = Wrappers.lambdaQuery(getModelClass());
        consumer.accept(queryWrapper);
        return queryList(queryWrapper);
    }

    @Override
    public List<M> queryList(@Nonnull final Wrapper<M> query) {
        return getMapper().selectList(query);
    }

    @Override
    public PageList<M> queryForPage(@Nullable final PagingQuery page, @Nullable final Consumer<LambdaQueryWrapper<M>> consumer) {
        final LambdaQueryWrapper<M> queryWrapper = Wrappers.lambdaQuery(getModelClass());
        if (Objects.nonNull(consumer)) {
            consumer.accept(queryWrapper);
        }
        return queryForPage(page, queryWrapper);
    }

    @Override
    public PageList<M> queryForPage(@Nullable final PagingQuery page, @Nullable final Wrapper<M> query) {
        final int idx = (Objects.isNull(page) || page.getPageIndex() <= 0) ? BasePageDTO.DEF_PAGE_INDEX : page.getPageIndex();
        final int size = (Objects.isNull(page) || page.getPageSize() <= 0) ? BasePageDTO.DEF_PAGE_SIZE : page.getPageSize();
        IPage<M> p = new Page<>(idx, size);
        p = getMapper().selectPage(p, query);
        return DataResult.of(p.getTotal(), p.getRecords());
    }

    protected <R> PageList<R> mapping(@Nonnull final PageList<M> pageList, @Nonnull final Function<M, R> convert) {
        final List<M> items;
        if (!CollectionUtils.isEmpty(items = pageList.getRows())) {
            return DataResult.of(pageList.getTotal(),
                    items.stream()
                            .map(convert)
                            .collect(Collectors.toList())
            );
        }
        return DataResult.of(pageList.getTotal(), Lists.newArrayList());
    }

    /**
     * 填充数据
     *
     * @param po 实体
     */
    protected <T extends Model<K>> void patchData(@Nonnull final T po) {
        this.setAutoId(po);
        this.setCreate(po);
        this.setUpdate(po);
        this.setStatus(po);
        this.setLogicDel(po);
    }

    protected <T extends Model<K>> void setAutoId(@Nonnull final T po) {
        if (Objects.isNull(po.getId()) || "".equals(po.getId())) {
            po.setId(genId());
        }
    }

    protected <T> void setCreate(@Nonnull final T po) {
        setUser(po, PoConstant.CREATED_BY);
        setFieldValue(po, PoConstant.CREATED_AT, cls -> new Date());
    }

    protected <T> void setUpdate(@Nonnull final T po) {
        setUser(po, PoConstant.UPDATED_BY);
        setFieldValue(po, PoConstant.UPDATED_AT, cls -> new Date());
    }

    protected void setUpdate(@Nonnull final LambdaUpdateWrapper<M> updateWrapper) {
        final String sqlSet = updateWrapper.getSqlSet();
        if (!Strings.isNullOrEmpty(sqlSet)) {
            final Map<String, Object> params = Maps.newHashMap();
            //更新时间
            final String updateAt = poPoFieldHelper.getColumn(PoConstant.UPDATED_AT);
            if (!Strings.isNullOrEmpty(updateAt) && !sqlSet.contains(updateAt)) {
                params.put(updateAt, new Date());
            }
            //更新用户
            Optional.ofNullable(SecurityUtils.getPrincipal())
                    .ifPresent(u -> {
                        final String updateBy = poPoFieldHelper.getColumn(PoConstant.UPDATED_BY);
                        if (!Strings.isNullOrEmpty(updateBy) && !sqlSet.contains(updateBy)) {
                            params.put(updateBy, u.getId());
                        }
                    });
            //设置更新数据
            if (!CollectionUtils.isEmpty(params)) {
                MybatisPlusUtils.buildFieldMap(params, getModelClass(), updateWrapper, null);
            }
        }
    }

    private <T> void setUser(@Nonnull final T po, @Nonnull final PoConstant pc) {
        Optional.ofNullable(SecurityUtils.getPrincipal())
                .ifPresent(u -> {
                    final Field userField = poPoFieldHelper.getField(pc);
                    if (Objects.nonNull(userField)) {
                        setFieldValue(po, userField, u.getId());
                    }
                });
    }

    protected <T> void setStatus(@Nonnull final T po) {
        //状态
        setFieldValue(po, PoConstant.STATUS, cls -> {
            final Status enable = Status.ENABLE;
            if (cls == Status.class) {
                return enable;
            }
            return enable.getVal();
        });
    }

    protected <T> void setLogicDel(@Nonnull final T po) {
        //初始化逻辑删除
        final Field logicDelField = this.poPoFieldHelper.getField(PoConstant.DELETED_AT);
        if (Objects.nonNull(logicDelField) && logicDelField.isAnnotationPresent(TableLogic.class)) {
            final String defVal = logicDelField.getAnnotation(TableLogic.class).value();
            if (!Strings.isNullOrEmpty(defVal)) {
                final Class<?> logicDelType = logicDelField.getType();
                if (logicDelType == String.class) {
                    this.setFieldValue(po, logicDelField, defVal);
                    return;
                }
                if (logicDelType == Integer.class || logicDelType == Long.class) {
                    this.setFieldValue(po, logicDelField, Integer.parseInt(defVal));
                }
            }
        }
    }

    private <T, R> void setFieldValue(@Nonnull final T po, @Nullable final PoConstant pc, @Nonnull final Function<Class<?>, R> valHandler) {
        Optional.ofNullable(pc)
                .map(poPoFieldHelper::getField)
                .ifPresent(field -> Optional.ofNullable(valHandler.apply(field.getType()))
                        .ifPresent(val -> setFieldValue(po, field, val))
                );
    }

    private <T> void setFieldValue(@Nonnull final T po, @Nullable final Field field, @Nonnull final Object val) {
        if (Objects.nonNull(field)) {
            final Object old = ReflectionUtils.getField(field, po);
            if (Objects.isNull(old)) {
                field.setAccessible(true);
                //设置新值
                ReflectionUtils.setField(field, po, val);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(@Nonnull final M po) {
        //补充数据
        patchData(po);
        //插入数据
        final int ret = getMapper().insert(po);
        //返回
        return SqlHelper.retBool(ret);
    }

    protected boolean addOrUpdate(@Nonnull final M po) {
        //补充数据
        patchData(po);
        //插入数据
        final List<M> pos = Lists.newArrayList();
        pos.add(po);
        final int ret = getMapper().batchAddOrUpdate(pos);
        //新增结果
        return SqlHelper.retBool(ret);
    }

    @SuppressWarnings({"deprecation"})
    private String getSqlStatement(@Nonnull final SqlMethod sqlMethod) {
        return SqlHelper.table(getModelClass())
                .getSqlStatement(sqlMethod.getMethod());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAdd(@Nonnull final Collection<M> items) {
        if (!CollectionUtils.isEmpty(items)) {
            final List<M> rows = items.stream()
                    .filter(Objects::nonNull)
                    .peek(this::patchData)
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(rows)) {
                final String sqlStatement = getSqlStatement(SqlMethod.INSERT_ONE);
                return batchHandler(rows, (session, po) -> session.insert(sqlStatement, po));
            }
        }
        return false;
    }

    protected boolean batchAddOrUpdate(@Nonnull final Collection<M> items) {
        if (CollectionUtils.isEmpty(items)) {
            return false;
        }
        final List<M> rows = items.stream()
                .filter(Objects::nonNull)
                .peek(this::patchData)
                .collect(Collectors.toList());
        return batchHandler(rows, pos -> {
            final int ret = getMapper().batchAddOrUpdate(pos);
            log.debug("batchAddOrUpdate=> {}", ret);
        });
    }

    private boolean batchHandler(@Nonnull final Collection<M> pos, @Nonnull final Consumer<List<M>> handler) {
        int count = 0;
        if (!CollectionUtils.isEmpty(pos)) {
            final List<M> items = Lists.newArrayList(pos);
            final int totals = pos.size();
            int idx = 0;
            while (count < totals) {
                final int start = (idx * BATCH_SIZE), end = Math.min(start + BATCH_SIZE, totals);
                final List<M> rows = items.subList(start, end);
                count += rows.size();
                //批处理
                handler.accept(rows);
                //分页计数
                idx++;
            }
        }
        return count > 0;
    }

    protected boolean batchHandler(@Nonnull final Collection<M> pos, @Nonnull final BiConsumer<SqlSession, M> handler) {
        final AtomicInteger refIdx = new AtomicInteger(0);
        try (final SqlSession session = SqlHelper.sqlSessionBatch(getModelClass())) {
            pos.stream()
                    .filter(Objects::nonNull)
                    .forEach(po -> {
                        handler.accept(session, po);
                        final int total = refIdx.incrementAndGet();
                        if (total >= 1 && (total % BATCH_SIZE == 0)) {
                            session.flushStatements();
                        }
                    });
            if (refIdx.get() > 0) {
                session.flushStatements();
            }
        }
        return refIdx.get() > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean modify(@Nonnull final K id, @Nonnull final M po) {
        po.setId(id);
        setUpdate(po);
        return SqlHelper.retBool(getMapper().updateById(po));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean modify(@Nonnull final Consumer<LambdaUpdateWrapper<M>> consumer) {
        final LambdaUpdateWrapper<M> updateWrapper = Wrappers.lambdaUpdate(getModelClass());
        consumer.accept(updateWrapper);
        return modify(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean modify(@Nonnull final LambdaUpdateWrapper<M> updateWrapper) {
        setUpdate(updateWrapper);
        return SqlHelper.retBool(getMapper().update(null, updateWrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchModify(@Nonnull final Collection<M> items) {
        if (!CollectionUtils.isEmpty(items)) {
            final String sqlStatement = getSqlStatement(SqlMethod.UPDATE_BY_ID);
            return batchHandler(items, (session, po) -> {
                setUpdate(po);
                final MapperMethod.ParamMap<M> param = new MapperMethod.ParamMap<>();
                param.put(Constants.ENTITY, po);
                session.update(sqlStatement, param);
            });
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(@Nonnull final K id) {
        return delete(id, false);
    }

    protected boolean delete(@Nonnull final K id, final boolean physicalDel) {
        final ModelMapper<M, K> mapper = getMapper();
        if (physicalDel) {
            return SqlHelper.retBool(mapper.physicalDelete(Lists.newArrayList(id)));
        }
        return SqlHelper.retBool(mapper.deleteById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(@Nonnull final List<K> ids) {
        return delete(ids, false);
    }

    protected boolean delete(@Nonnull final List<K> ids, final boolean physicalDel) {
        final ModelMapper<M, K> mapper = getMapper();
        if (physicalDel) {
            return SqlHelper.retBool(mapper.physicalDelete(ids));
        }
        return SqlHelper.retBool(mapper.deleteBatchIds(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(@Nonnull final Consumer<LambdaQueryWrapper<M>> consumer) {
        final LambdaQueryWrapper<M> queryWrapper = Wrappers.lambdaQuery(getModelClass());
        consumer.accept(queryWrapper);
        return delete(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(@Nonnull final Wrapper<M> wrapper) {
        return SqlHelper.retBool(getMapper().delete(wrapper));
    }

    private <R> R mappingHandler(@Nonnull final Function<BeanMapping, R> handler) {
        return Optional.ofNullable(beanMapping)
                .map(handler)
                .orElse(null);
    }

    @Override
    public <T, R> R mapping(@Nullable final T data, @Nonnull final Class<R> cls) {
        return mappingHandler(bm -> bm.mapping(data, cls));
    }

    @Override
    public <T, R> List<R> mapping(@Nullable final List<T> items, @Nonnull final Class<R> cls) {
        return mappingHandler(bm -> bm.mapping(items, cls));
    }

    @Override
    public <T extends Serializable, R extends Serializable> PageList<R> mapping(@Nullable final PageList<T> pageList, @Nonnull final Class<R> cls) {
        return mappingHandler(bm -> bm.mapping(pageList, cls));
    }
}
