package top.zenyoung.orm;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
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
public class OrmAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        final MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //分页
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        //乐观锁
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        //防止全表更新或删除
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

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
