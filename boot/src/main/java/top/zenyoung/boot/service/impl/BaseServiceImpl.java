package top.zenyoung.boot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import top.zenyoung.boot.service.BaseService;
import top.zenyoung.boot.service.BeanMappingService;
import top.zenyoung.common.mapping.BeanMapping;
import top.zenyoung.common.paging.PageList;

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
    public <T, MT> MT mapping(final T data, final Class<MT> mtClass) {
        return mappingService.mapping(data, mtClass);
    }

    @Override
    public <T, MT> List<MT> mapping(final List<T> items, final Class<MT> mtClass) {
        return mappingService.mapping(items, mtClass);
    }

    @Override
    public <T, MT> PageList<MT> mapping(final PageList<T> pageList, final Class<MT> mtClass) {
        return mappingService.mapping(pageList, mtClass);
    }
}
