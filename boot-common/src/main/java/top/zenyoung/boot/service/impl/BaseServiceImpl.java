package top.zenyoung.boot.service.impl;

import lombok.extern.slf4j.Slf4j;
import top.zenyoung.boot.service.BaseService;
import top.zenyoung.common.mapping.BeanMapping;
import top.zenyoung.common.mapping.BeanMappingDefault;
import top.zenyoung.common.paging.PageList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

/**
 * 服务基础接口实现类
 *
 * @author young
 */
@Slf4j
public abstract class BaseServiceImpl implements BaseService {
    private static final BeanMapping beanMapping = BeanMappingDefault.INSTANCE;

    private <R> R mappingHandler(@Nonnull final Function<BeanMapping, R> handler) {
        return Optional.ofNullable(beanMapping)
                .map(handler)
                .orElse(null);
    }

    @Override
    public <T, R> R mapping(@Nullable final T data, @Nonnull final Class<R> rClass) {
        return mappingHandler(bms -> bms.mapping(data, rClass));
    }

    @Override
    public <T, R> Collection<R> mapping(@Nullable final Collection<T> items, @Nonnull final Class<R> rClass) {
        return mappingHandler(bms -> bms.mapping(items, rClass));
    }

    @Override
    public <T extends Serializable, R extends Serializable> PageList<R> mapping(@Nullable final PageList<T> pageList,
                                                                                @Nonnull final Class<R> rClass) {
        return mappingHandler(bms -> bms.mapping(pageList, rClass));
    }
}
