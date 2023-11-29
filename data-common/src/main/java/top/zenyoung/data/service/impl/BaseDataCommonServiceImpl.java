package top.zenyoung.data.service.impl;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import top.zenyoung.common.mapping.BeanMapping;
import top.zenyoung.common.mapping.BeanMappingDefault;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.sequence.IdSequence;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 数据服务公共实现基类
 *
 * @author young
 */
public abstract class BaseDataCommonServiceImpl<K extends Serializable> implements BeanMapping {
    private static final BeanMapping beanMapping = BeanMappingDefault.INSTANCE;
    private final Map<Integer, Class<?>> genericTypeCache = Maps.newConcurrentMap();
    @Autowired(required = false)
    private IdSequence idSequence;

    /**
     * ID生成器处理
     *
     * @param handler 生成处理器
     * @return ID生成结果
     */
    protected K idSequenceHandler(@Nonnull final Function<IdSequence, K> handler) {
        if (Objects.isNull(idSequence)) {
            return null;
        }
        return handler.apply(idSequence);
    }

    /**
     * 获取主键类型
     *
     * @return 主键类型
     */
    protected Class<?> getGenericKeyType() {
        final int index = 0;
        return genericTypeCache.computeIfAbsent(index, idx -> {
            final Class<?>[] cls = GenericTypeResolver.resolveTypeArguments(getClass(), BaseDataCommonServiceImpl.class);
            if (cls != null && cls.length > 0) {
                return cls[idx];
            }
            return null;
        });
    }

    @SuppressWarnings({"unchecked"})
    protected K genId() {
        return idSequenceHandler(sequence -> {
            final Long id = sequence.nextId();
            final Class<?> cls = getGenericKeyType();
            if (Objects.nonNull(cls)) {
                if (cls == Long.class) {
                    return (K) id;
                }
                if (cls == String.class) {
                    return (K) (id + "");
                }
            }
            return null;
        });
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
