package top.zenyoung.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import top.zenyoung.common.paging.PagingResult;
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

    protected BeanMappingService getMappingService() {
        return context.getBean(BeanMappingService.class);
    }

    @Override
    public <T, MT> MT mapping(@Nullable final T data, @Nonnull final Class<MT> tClass) {
        return getMappingService().mapping(data, tClass);
    }

    @Override
    public <T, MT> List<MT> mapping(@Nullable final List<T> ts, @Nonnull final Class<MT> tClass) {
        return getMappingService().mapping(ts, tClass);
    }

    @Override
    public <T extends Serializable, MT extends Serializable> PagingResult<MT> mapping(@Nullable final PagingResult<T> pageList, @Nonnull final Class<MT> tClass) {
        return getMappingService().mapping(pageList, tClass);
    }
}
