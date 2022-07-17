package top.zenyoung.orm.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.boot.service.impl.BaseServiceImpl;
import top.zenyoung.boot.util.SecurityUtils;
import top.zenyoung.common.model.Status;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.orm.constant.PoConstants;
import top.zenyoung.orm.mapper.BaseMapper;
import top.zenyoung.orm.model.BasePO;
import top.zenyoung.orm.service.BaseOrmService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * ORM-操作服务接口实现基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseOrmServiceImpl<PO extends BasePO<ID>, ID extends Serializable> extends BaseServiceImpl implements BaseOrmService<PO, ID> {
    protected static final int BATCH_SIZE = 500;

    @Autowired(required = false)
    private IdSequence idSequence;

    private Class<?> getGenericType(final int index) {
        return ReflectionKit.getSuperClassGenericType(getClass(), BaseOrmService.class, index);
    }

    /**
     * 生成主键ID
     *
     * @return 主键ID
     */
    @SuppressWarnings({"unchecked"})
    protected ID genId() {
        final Long id = this.idSequence.nextId();
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
        return getMapper().selectById(id);
    }

    @Override
    public PO getOne(@Nonnull final Wrapper<PO> query) {
        return getMapper().selectOne(query);
    }

    @Override
    public int count(@Nonnull final Wrapper<PO> query) {
        return (int) SqlHelper.retCount(getMapper().selectCount(query));
    }

    @Override
    public List<PO> queryList(@Nonnull final Wrapper<PO> query) {
        return getMapper().selectList(query);
    }

    @Override
    public PageList<PO> queryForPage(@Nullable final Integer pageNum, @Nullable final Integer pageSize, @Nullable final Wrapper<PO> query) {
        final int idx = (Objects.isNull(pageNum) || pageNum <= 0) ? BasePageDTO.DEF_PAGE_INDEX : pageNum;
        final int size = (Objects.isNull(pageSize) || pageSize <= 0) ? BasePageDTO.DEF_PAGE_SIZE : pageSize;
        IPage<PO> page = new Page<>(idx, size);
        page = getMapper().selectPage(page, query);
        return DataResult.of(page.getTotal(), page.getRecords());
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
    }

    protected void setAutoId(@Nonnull final PO po) {
        if (Objects.isNull(po.getId()) || "".equals(po.getId())) {
            po.setId(genId());
        }
    }

    protected void setCreate(@Nonnull final PO po) {
        setUser(po, PoConstants.CREATE_BY);
        setFieldValue(po, PoConstants.CREATE_AT, new Date());
    }

    protected void setUpdate(@Nonnull final PO po) {
        setUser(po, PoConstants.UPDATE_BY);
        setFieldValue(po, PoConstants.UPDATE_AT, new Date());
    }

    protected void setUpdate(@Nonnull final LambdaUpdateWrapper<PO> updateWrapper) {
        final String sqlSet = updateWrapper.getSqlSet();
        if (!Strings.isNullOrEmpty(sqlSet)) {
            final Class<?> poClass = getPoClass();
            final Map<String, Field> fieldMaps = ReflectionKit.getFieldMap(poClass);
            //更新时间
            if (!sqlSet.contains(PoConstants.UPDATE_AT)) {
                setFieldValue(updateWrapper, fieldMaps, PoConstants.UPDATE_AT, new Date());
            }
            //更新用户
            if (!sqlSet.contains(PoConstants.UPDATE_BY)) {
                setUser(updateWrapper, fieldMaps, PoConstants.UPDATE_BY);
            }
        }
    }

    private void setUser(@Nonnull final PO po, @Nonnull final String field) {
        if (Strings.isNullOrEmpty(field)) {
            return;
        }
        try {
            SecurityUtils.getUserOpt().ifPresent(u -> setFieldValue(po, field, u.getId()));
        } catch (Throwable e) {
            log.warn("setUser(po: {},field: {})-exp: {}", po, field, e.getMessage());
        }
    }

    private void setUser(@Nonnull final LambdaUpdateWrapper<PO> updateWrapper, @Nonnull final Map<String, Field> fieldMaps, @Nonnull final String field) {
        if (Strings.isNullOrEmpty(field)) {
            return;
        }
        try {
            SecurityUtils.getUserOpt().ifPresent(u -> setFieldValue(updateWrapper, fieldMaps, field, u.getId()));
        } catch (Throwable e) {
            log.warn("setUser(updateWrapper: {},field: {})-exp: {}", updateWrapper, field, e.getMessage());
        }
    }

    protected void setStatus(@Nonnull final PO po) {
        //状态
        setFieldValue(po, PoConstants.STATUS, Status.Enable.getVal());
        //逻辑删除
        setFieldValue(po, PoConstants.LOGIC_DEL, Status.Disable.getVal());
    }

    private void setFieldValue(@Nonnull final PO po, @Nonnull final String field, @Nonnull final Object val) {
        if (Strings.isNullOrEmpty(field)) {
            return;
        }
        try {
            final Field f = ReflectionUtils.findField(po.getClass(), field);
            if (Objects.nonNull(f)) {
                f.setAccessible(true);
                final Object old = ReflectionUtils.getField(f, po);
                if (Objects.isNull(old)) {
                    //设置新值
                    ReflectionUtils.setField(f, po, val);
                }
            }
        } catch (Throwable e) {
            log.warn("setFieldValue(po: {},field: {},val: {})-exp: {}", po, field, val, e.getMessage());
        }
    }

    private void setFieldValue(@Nonnull final LambdaUpdateWrapper<PO> updateWrapper, @Nonnull final Map<String, Field> fieldMaps,
                               @Nonnull final String field, @Nonnull final Object val) {
        if (Strings.isNullOrEmpty(field) || !fieldMaps.containsKey(field)) {
            return;
        }
        try {
            final Field f = fieldMaps.get(field);
            if (Objects.nonNull(f)) {
                f.setAccessible(true);
                final AtomicBoolean has = new AtomicBoolean(true);
                final SFunction<PO, ?> sf = po -> {
                    try {
                        return f.get(po);
                    } catch (Throwable e) {
                        has.set(false);
                        log.warn("setUpdate[f: {}]-exp: {}", f, e);
                    }
                    return null;
                };
                if (has.get()) {
                    updateWrapper.set(sf, val);
                }
            }
        } catch (Throwable e) {
            log.warn("setFieldValue(updateWrapper: {},field: {},val: {})-exp: {}", updateWrapper, field, val, e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PO add(@Nonnull final PO po) {
        //补充数据
        patchData(po);
        //插入数据
        getMapper().insert(po);
        //返回
        return po;
    }

    /**
     * 获取PO class
     *
     * @return class
     */
    protected Class<?> getPoClass() {
        return getGenericType(0);
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
            final Class<?> poClass = getPoClass();
            final Class<?> mapperClass = getMapper().getClass();
            final Log l = new Slf4jImpl(getClass().getName());
            return SqlHelper.saveOrUpdateBatch(poClass, mapperClass, l, rows, BATCH_SIZE, (s, p) -> true, null);
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int modify(@Nonnull final ID id, @Nonnull final PO po) {
        po.setId(id);
        setUpdate(po);
        return getMapper().updateById(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int modify(@Nonnull final LambdaUpdateWrapper<PO> updateWrapper) {
        setUpdate(updateWrapper);
        return getMapper().update(null, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchModify(@Nonnull final Collection<PO> items) {
        if (!CollectionUtils.isEmpty(items)) {
            final Class<?> poClass = getPoClass();
            final Class<?> mapperClass = getMapper().getClass();
            final Log l = new Slf4jImpl(getClass().getName());
            final String sqlStatement = SqlHelper.getSqlStatement(mapperClass, SqlMethod.UPDATE_BY_ID);
            return SqlHelper.executeBatch(poClass, l, items, BATCH_SIZE, (session, po) -> {
                final MapperMethod.ParamMap<PO> param = new MapperMethod.ParamMap<>();
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
        return SqlHelper.retBool(getMapper().deleteById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(@Nonnull final Collection<ID> ids) {
        return SqlHelper.retBool(getMapper().deleteBatchIds(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(@Nonnull final Wrapper<PO> wrapper) {
        return SqlHelper.retBool(getMapper().delete(wrapper));
    }
}
