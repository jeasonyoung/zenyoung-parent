package top.zenyoung.data.service.impl;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import top.zenyoung.common.mapping.BeanMapping;
import top.zenyoung.common.mapping.BeanMappingDefault;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.sequence.Sequence;
import top.zenyoung.data.entity.Model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 数据服务公共实现基类
 *
 * @param <M> 数据模型类型
 * @param <K> 数据主键类型
 */
public abstract class BaseDataCommonServiceImpl<M extends Model<K>, K extends Serializable> implements BeanMapping {
    private static final BeanMapping beanMapping = BeanMappingDefault.INSTANCE;
    private final Map<Integer, Class<?>> genericTypeCache = Maps.newConcurrentMap();
    /**
     * 注入ID生成器
     */
    @Autowired(required = false)
    private Sequence idSequence;

    /**
     * 获取泛型类型
     *
     * @param index 泛型索引
     * @return 泛型类型
     */
    private Class<?> getGenericType(final int index) {
        return genericTypeCache.computeIfAbsent(index, idx -> {
            final Class<?>[] cls = GenericTypeResolver.resolveTypeArguments(getClass(), BaseDataCommonServiceImpl.class);
            if (cls != null && cls.length > 0) {
                return cls[idx];
            }
            return null;
        });
    }

    /**
     * 获取数据模型类型
     *
     * @return 数据模型类型
     */
    @SuppressWarnings({"unchecked"})
    protected Class<M> getModelClass() {
        return (Class<M>) getGenericType(0);
    }

    /**
     * 获取数据主键类型
     *
     * @return 数据主键类型
     */
    @SuppressWarnings({"unchecked"})
    protected Class<K> getKeyClass() {
        return (Class<K>) getGenericType(1);
    }

    /**
     * 生成主键ID
     *
     * @return 主键ID
     */
    @SuppressWarnings({"unchecked"})
    protected K genId() {
        final long id = idSequence.nextId();
        final Class<?> cls = getKeyClass();
        if (Objects.nonNull(cls)) {
            if (cls == Long.class) {
                return (K) cls.cast(id);
            }
            if (cls == String.class) {
                return (K) cls.cast(id + "");
            }
        }
        return null;
    }

    @Override
    public <T, R> R mapping(@Nullable final T data, @Nonnull final Class<R> cls) {
        return beanMapping.mapping(data, cls);
    }

    @Override
    public <T, R> List<R> mapping(@Nullable final List<T> items, @Nonnull final Class<R> cls) {
        return beanMapping.mapping(items, cls);
    }

    @Override
    public <T extends Serializable, R extends Serializable> PageList<R> mapping(@Nullable final PageList<T> pageList, @Nonnull final Class<R> cls) {
        return beanMapping.mapping(pageList, cls);
    }
}
