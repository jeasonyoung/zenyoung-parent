package top.zenyoung.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.common.util.BeanCacheUtils;
import top.zenyoung.service.BaseService;
import top.zenyoung.service.BeanMappingService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

/**
 * 服务基类实现
 *
 * @author young
 */
public class BaseServiceImpl implements BaseService {
    @Autowired
    private ApplicationContext context;

    /**
     * 获取Spring上下文对象
     *
     * @return Spring上下文对象
     */
    protected ApplicationContext getContext() {
        return this.context;
    }

    @Override
    public <T, MT> MT mapping(@Nullable final T data, @Nonnull final Class<MT> tClass) {
        return BeanCacheUtils.function(context, BeanMappingService.class, bean -> bean.mapping(data, tClass));
    }

    @Override
    public <T, MT> List<MT> mapping(@Nullable final List<T> items, @Nonnull final Class<MT> tClass) {
        return BeanCacheUtils.function(context, BeanMappingService.class, bean -> bean.mapping(items, tClass));
    }

    @Override
    public <T extends Serializable, MT extends Serializable> PagingResult<MT> mapping(@Nullable final PagingResult<T> pageList, @Nonnull final Class<MT> tClass) {
        return BeanCacheUtils.function(context, BeanMappingService.class, bean -> bean.mapping(pageList, tClass));
    }
}
