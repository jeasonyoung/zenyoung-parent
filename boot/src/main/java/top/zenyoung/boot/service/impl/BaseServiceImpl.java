package top.zenyoung.boot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import top.zenyoung.boot.service.BaseService;
import top.zenyoung.boot.service.BeanMappingService;
import top.zenyoung.common.mapping.BeanMapping;
import top.zenyoung.common.paging.PageList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 服务基础接口实现类
 *
 * @author young
 */
public class BaseServiceImpl implements BaseService, BeanMapping {

    @Autowired
    private BeanMappingService mappingService;

    @Override
    public <T, MT> MT mapping(@Nullable final T data, @Nonnull final Class<MT> mtClass) {
        return mappingService.mapping(data, mtClass);
    }

    @Override
    public <T, MT> List<MT> mapping(@Nullable final List<T> items, @Nonnull final Class<MT> mtClass) {
        return mappingService.mapping(items, mtClass);
    }

    @Override
    public <T, MT> PageList<MT> mapping(@Nullable final PageList<T> pageList, @Nonnull final Class<MT> mtClass) {
        return mappingService.mapping(pageList, mtClass);
    }
}
