package top.zenyoung.orm;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.CollectionUtils;
import top.zenyoung.orm.config.MybatisPlusConfig;
import top.zenyoung.orm.injector.BatchAddOrUpdateMethod;
import top.zenyoung.orm.injector.PhysicalDeleteMethod;
import top.zenyoung.orm.injector.SelectPhysicalByIdMethod;
import top.zenyoung.orm.injector.SelectPhysicalByIdsMethod;

import java.util.List;

/**
 * Orm 自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@Import({MybatisPlusConfig.class})
public class OrmAutoConfiguration {

    @Bean
    public ISqlInjector sqlInjector() {
        return new DefaultSqlInjector() {

            @Override
            public List<AbstractMethod> getMethodList(final Class<?> mapperClass, final TableInfo tableInfo) {
                final List<AbstractMethod> methods = super.getMethodList(mapperClass, tableInfo);
                if (!CollectionUtils.isEmpty(methods)) {
                    //根据ID加载数据(包括逻辑删除的)
                    methods.add(new SelectPhysicalByIdMethod());
                    //根据ID集合查询数据(包括被逻辑删除的)
                    methods.add(new SelectPhysicalByIdsMethod());
                    //重复键插入变更新
                    methods.add(new BatchAddOrUpdateMethod());
                    //物理删除
                    methods.add(new PhysicalDeleteMethod());
                }
                return methods;
            }
        };
    }
}
