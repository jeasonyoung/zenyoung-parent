package top.zenyoung.boot.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import top.zenyoung.boot.service.BaseService;
import top.zenyoung.common.mapping.BeanMapping;
import top.zenyoung.common.paging.PageList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 服务基础接口实现类
 *
 * @author young
 */
@Slf4j
public class BaseServiceImpl implements BaseService, BeanMapping {
    @Autowired(required = false)
    private BeanMapping beanMapping;

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
    public <T, R> List<R> mapping(@Nullable final List<T> items, @Nonnull final Class<R> rClass) {
        return mappingHandler(bms -> bms.mapping(items, rClass));
    }

    @Override
    public <T extends Serializable, R extends Serializable> PageList<R> mapping(@Nullable final PageList<T> pageList,
                                                                                @Nonnull final Class<R> rClass) {
        return mappingHandler(bms -> bms.mapping(pageList, rClass));
    }
}
