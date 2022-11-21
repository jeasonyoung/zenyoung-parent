package top.zenyoung.orm.service.impl;

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
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import top.zenyoung.boot.service.impl.BaseServiceImpl;
import top.zenyoung.boot.util.SecurityUtils;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.common.model.Status;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.paging.PagingQuery;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.orm.enums.PoConstant;
import top.zenyoung.orm.mapper.BaseMapper;
import top.zenyoung.orm.model.BasePO;
import top.zenyoung.orm.model.PoFieldHelper;
import top.zenyoung.orm.service.BaseOrmService;
import top.zenyoung.orm.util.MybatisPlusUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ORM-操作服务接口实现基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseOrmServiceImpl<PO extends BasePO<ID>, ID extends Serializable> extends BaseServiceImpl implements BaseOrmService<PO, ID>, InitializingBean {
    protected static final int BATCH_SIZE = 500;
    private final Map<Integer, Class<?>> clsMaps = Maps.newConcurrentMap();
    private final PoFieldHelper<PO> poPoFieldHelper = PoFieldHelper.of(this.getModelClass());
    @Autowired(required = false)
    private IdSequence idSequence;

    private Class<?> getGenericType(final int index) {
        return clsMaps.computeIfAbsent(index, idx -> ReflectionKit.getSuperClassGenericType(getClass(), BaseOrmServiceImpl.class, idx));
    }

    @SuppressWarnings({"unchecked"})
    protected final Class<PO> getModelClass() {
        return (Class<PO>) getGenericType(0);
    }

    /**
     * 生成主键ID
     *
     * @return 主键ID
     */
    @SuppressWarnings({"unchecked"})
    protected ID genId() {
        if (Objects.isNull(idSequence)) {
            return null;
        }
        final Long id = idSequence.nextId();
        final Class<ID> cls = (Class<ID>) getGenericType(1);
        if (cls == Long.class) {
            return cls.cast(id);
        }
        if (cls == String.class) {
            return cls.cast(id + "");
        }
        return cls.cast(id);
    }

    /**
     * 获取Mapper
     *
     * @return Mapper
     */
    @Nonnull
    protected abstract BaseMapper<PO, ID> getMapper();

    @Override
    public PO getById(@Nonnull final ID id) {
        return getById(id, false);
    }

    protected PO getById(@Nonnull final ID id, final boolean logicDel) {
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
    public PO getOne(@Nonnull final Consumer<LambdaQueryWrapper<PO>> consumer) {
        final LambdaQueryWrapper<PO> queryWrapper = Wrappers.lambdaQuery(getModelClass());
        consumer.accept(queryWrapper);
        return getOne(queryWrapper);
    }

    @Override
    public PO getOne(@Nonnull final Wrapper<PO> query) {
        return getMapper().selectOne(query);
    }

    @Override
    public int count(@Nonnull final Consumer<LambdaQueryWrapper<PO>> consumer) {
        final LambdaQueryWrapper<PO> queryWrapper = Wrappers.lambdaQuery(getModelClass());
        consumer.accept(queryWrapper);
        return count(queryWrapper);
    }

    @Override
    public int count(@Nonnull final Wrapper<PO> query) {
        return (int) SqlHelper.retCount(getMapper().selectCount(query));
    }

    @Override
    public List<PO> queryList(@Nonnull final Consumer<LambdaQueryWrapper<PO>> consumer) {
        final LambdaQueryWrapper<PO> queryWrapper = Wrappers.lambdaQuery(getModelClass());
        consumer.accept(queryWrapper);
        return queryList(queryWrapper);
    }

    @Override
    public List<PO> queryList(@Nonnull final Wrapper<PO> query) {
        return getMapper().selectList(query);
    }

    @Override
    public PageList<PO> queryForPage(@Nullable final PagingQuery page, @Nullable final Consumer<LambdaQueryWrapper<PO>> consumer) {
        final LambdaQueryWrapper<PO> queryWrapper = Wrappers.lambdaQuery(getModelClass());
        if (Objects.nonNull(consumer)) {
            consumer.accept(queryWrapper);
        }
        return queryForPage(page, queryWrapper);
    }

    @Override
    public PageList<PO> queryForPage(@Nullable final PagingQuery page, @Nullable final Wrapper<PO> query) {
        final int idx = (Objects.isNull(page) || page.getPageIndex() <= 0) ? BasePageDTO.DEF_PAGE_INDEX : page.getPageIndex();
        final int size = (Objects.isNull(page) || page.getPageSize() <= 0) ? BasePageDTO.DEF_PAGE_SIZE : page.getPageSize();
        IPage<PO> p = new Page<>(idx, size);
        p = getMapper().selectPage(p, query);
        return DataResult.of(p.getTotal(), p.getRecords());
    }

    protected <R> PageList<R> mapping(@Nonnull final PageList<PO> pageList, @Nonnull final Function<PO, R> convert) {
        final List<PO> items;
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
    protected void patchData(@Nonnull final PO po) {
        this.setAutoId(po);
        this.setCreate(po);
        this.setUpdate(po);
        this.setStatus(po);
        //初始化逻辑删除
        final Field logicDelField = this.poPoFieldHelper.getField(PoConstant.LogicDel);
        if (Objects.nonNull(logicDelField) && logicDelField.isAnnotationPresent(TableLogic.class)) {
            try {
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
            } catch (Throwable e) {
                log.warn("patchData(po: {})-初始化逻辑删除值失败", po);
            }
        }
    }

    protected void setAutoId(@Nonnull final PO po) {
        if (Objects.isNull(po.getId()) || "".equals(po.getId())) {
            po.setId(genId());
        }
    }

    protected <T> void setCreate(@Nonnull final T po) {
        setUser(po, PoConstant.CreateBy);
        setFieldValue(po, PoConstant.CreateAt, new Date());
    }

    protected <T> void setUpdate(@Nonnull final T po) {
        setUser(po, PoConstant.UpdateBy);
        setFieldValue(po, PoConstant.UpdateAt, new Date());
    }

    protected void setUpdate(@Nonnull final LambdaUpdateWrapper<PO> updateWrapper) {
        final String sqlSet = updateWrapper.getSqlSet();
        if (!Strings.isNullOrEmpty(sqlSet)) {
            final Map<String, Object> params = Maps.newHashMap();
            //更新时间
            final String updateAt = poPoFieldHelper.getColumn(PoConstant.UpdateAt);
            if (!Strings.isNullOrEmpty(updateAt) && !sqlSet.contains(updateAt)) {
                params.put(updateAt, new Date());
            }
            //更新用户
            final String updateBy = poPoFieldHelper.getColumn(PoConstant.UpdateBy);
            if (!Strings.isNullOrEmpty(updateBy) && !sqlSet.contains(updateBy)) {
                SecurityUtils.getUserOpt().ifPresent(u -> params.put(updateBy, u.getId()));
            }
            //
            if (!CollectionUtils.isEmpty(params)) {
                MybatisPlusUtils.buildFieldMap(params, getModelClass(), updateWrapper, null);
            }
        }
    }

    private <T> void setUser(@Nonnull final T po, @Nonnull final PoConstant pc) {
        try {
            final Field userField = poPoFieldHelper.getField(pc);
            if (Objects.nonNull(userField)) {
                SecurityUtils.getUserOpt().ifPresent(u -> setFieldValue(po, userField, u.getId()));
            }
        } catch (Throwable e) {
            log.warn("setUser(po: {},pc: {})-exp: {}", po, pc, e.getMessage());
        }
    }

    protected <T> void setStatus(@Nonnull final T po) {
        //状态
        setFieldValue(po, PoConstant.Status, Status.Enable.getVal());
        //逻辑删除
        setFieldValue(po, PoConstant.LogicDel, Status.Disable.getVal());
    }

    private <T> void setFieldValue(@Nonnull final T po, @Nullable final PoConstant pc, @Nonnull final Object val) {
        if (Objects.isNull(pc)) {
            return;
        }
        final Field field = poPoFieldHelper.getField(pc);
        if (Objects.nonNull(field)) {
            this.setFieldValue(po, field, val);
        }
    }

    private <T> void setFieldValue(@Nonnull final T po, @Nullable final Field field, @Nonnull final Object val) {
        try {
            if (Objects.isNull(field)) {
                return;
            }
            field.setAccessible(true);
            final Object old = ReflectionUtils.getField(field, po);
            if (Objects.isNull(old)) {
                //设置新值
                ReflectionUtils.setField(field, po, val);
            }
        } catch (Throwable e) {
            log.warn("setFieldValue(po: {},field: {},val: {})-exp: {}", po, field, val, e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(@Nonnull final PO po) {
        //补充数据
        patchData(po);
        //插入数据
        final int ret = getMapper().insert(po);
        //返回
        return SqlHelper.retBool(ret);
    }

    @Transactional(rollbackFor = Exception.class)
    protected boolean addOrUpdate(@Nonnull final PO po) {
        //补充数据
        patchData(po);
        //插入数据
        final int ret = getMapper().batchAddOrUpdate(Lists.newArrayList(po));
        //
        return SqlHelper.retBool(ret);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAdd(@Nonnull final Collection<PO> items) {
        if (CollectionUtils.isEmpty(items)) {
            return false;
        }
        final List<PO> rows = items.stream()
                .filter(Objects::nonNull)
                .peek(this::patchData)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(rows)) {
            final Class<?> poClass = getModelClass();
            final Class<?> mapperClass = getMapper().getClass();
            return batchAdd(poClass, mapperClass, rows);
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    protected boolean batchAddOrUpdate(@Nonnull final Collection<PO> items) {
        if (CollectionUtils.isEmpty(items)) {
            return false;
        }
        final List<PO> rows = items.stream()
                .filter(Objects::nonNull)
                .peek(this::patchData)
                .collect(Collectors.toList());
        return batchHandler(rows, pos -> {
            final int ret = getMapper().batchAddOrUpdate(pos);
            log.debug("batchAddOrUpdate=> {}", ret);
        });
    }

    private boolean batchHandler(@Nonnull final Collection<PO> pos, @Nonnull final Consumer<List<PO>> handler) {
        int count = 0;
        if (!CollectionUtils.isEmpty(pos)) {
            final List<PO> items = Lists.newArrayList(pos);
            final int totals = pos.size();
            int idx = 0;
            while (count < totals) {
                final int start = (idx * BATCH_SIZE), end = (start + BATCH_SIZE) > totals ? (totals - start) : (start + BATCH_SIZE);
                final List<PO> rows = items.subList(start, end);
                count += rows.size();
                //批处理
                handler.accept(rows);
                //分页计数
                idx++;
            }
        }
        return count > 0;
    }

    protected <T extends BasePO<?>> boolean batchAdd(@Nonnull final Class<?> poCls, @Nonnull final Class<?> mapperCls, @Nonnull final Collection<T> items) {
        if (!CollectionUtils.isEmpty(items)) {
            final Log l = new Slf4jImpl(getClass().getName());
            return SqlHelper.saveOrUpdateBatch(poCls, mapperCls, l, items, BATCH_SIZE, (s, p) -> true, null);
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean modify(@Nonnull final ID id, @Nonnull final PO po) {
        po.setId(id);
        setUpdate(po);
        return SqlHelper.retBool(getMapper().updateById(po));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean modify(@Nonnull final Consumer<LambdaUpdateWrapper<PO>> consumer) {
        final LambdaUpdateWrapper<PO> updateWrapper = Wrappers.lambdaUpdate(getModelClass());
        consumer.accept(updateWrapper);
        return modify(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean modify(@Nonnull final LambdaUpdateWrapper<PO> updateWrapper) {
        setUpdate(updateWrapper);
        return SqlHelper.retBool(getMapper().update(null, updateWrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchModify(@Nonnull final Collection<PO> items) {
        if (!CollectionUtils.isEmpty(items)) {
            final Class<?> poClass = getModelClass();
            final Class<?> mapperClass = getMapper().getClass();
            return batchModify(poClass, mapperClass, items);
        }
        return false;
    }

    protected <T extends BasePO<?>> boolean batchModify(@Nonnull final Class<?> poCls, @Nonnull final Class<?> mapperCls,
                                                        @Nonnull final Collection<T> items) {
        if (!CollectionUtils.isEmpty(items)) {
            final Log l = new Slf4jImpl(getClass().getName());
            final String sqlStatement = SqlHelper.getSqlStatement(mapperCls, SqlMethod.UPDATE_BY_ID);
            return SqlHelper.executeBatch(poCls, l, items, BATCH_SIZE, (session, po) -> {
                final MapperMethod.ParamMap<T> param = new MapperMethod.ParamMap<>();
                setUpdate(po);
                param.put(Constants.ENTITY, po);
                session.update(sqlStatement, param);
            });
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(@Nonnull final ID id) {
        return delete(id, false);
    }

    @Transactional(rollbackFor = Exception.class)
    protected boolean delete(@Nonnull final ID id, final boolean physicalDel) {
        final BaseMapper<PO, ID> mapper = getMapper();
        if (physicalDel) {
            return SqlHelper.retBool(mapper.physicalDelete(Lists.newArrayList(id)));
        }
        return SqlHelper.retBool(mapper.deleteById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(@Nonnull final List<ID> ids) {
        return delete(ids, false);
    }

    @Transactional(rollbackFor = Exception.class)
    protected boolean delete(@Nonnull final List<ID> ids, final boolean physicalDel) {
        final BaseMapper<PO, ID> mapper = getMapper();
        if (physicalDel) {
            return SqlHelper.retBool(mapper.physicalDelete(ids));
        }
        return SqlHelper.retBool(mapper.deleteBatchIds(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(@Nonnull final Consumer<LambdaQueryWrapper<PO>> consumer) {
        final LambdaQueryWrapper<PO> queryWrapper = Wrappers.lambdaQuery(getModelClass());
        consumer.accept(queryWrapper);
        return delete(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(@Nonnull final Wrapper<PO> wrapper) {
        return SqlHelper.retBool(getMapper().delete(wrapper));
    }
}
