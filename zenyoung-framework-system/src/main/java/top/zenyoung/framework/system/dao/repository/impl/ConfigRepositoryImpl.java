package top.zenyoung.framework.system.dao.repository.impl;

import com.google.common.base.Strings;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.data.repository.impl.BaseRepositoryImpl;
import top.zenyoung.framework.system.dao.entity.QConfigEntity;
import top.zenyoung.framework.system.dao.jpa.JpaConfig;
import top.zenyoung.framework.system.dao.repository.ConfigRepository;
import top.zenyoung.framework.system.dto.ConfigDTO;
import top.zenyoung.framework.system.dto.ConfigQueryDTO;
import top.zenyoung.service.BeanMappingService;

import javax.annotation.Nonnull;
import java.util.LinkedList;

/**
 * 参数管理-数据操作接口实现
 *
 * @author young
 */
@Service
@RequiredArgsConstructor
public class ConfigRepositoryImpl extends BaseRepositoryImpl implements ConfigRepository {
    private final JPAQueryFactory queryFactory;
    private final JpaConfig jpaConfig;
    private final BeanMappingService mappingService;

    @Override
    public PagingResult<ConfigDTO> query(@Nonnull final ConfigQueryDTO query) {
        return buildPagingQuery(query, q -> buildDslWhere(new LinkedList<BooleanExpression>() {
            {
                final QConfigEntity qConfigEntity = QConfigEntity.configEntity;
                //参数名
                if (!Strings.isNullOrEmpty(query.getName())) {
                    add(qConfigEntity.name.like("%" + query.getName() + "%"));
                }
                //状态
                if (query.getStatus() != null) {
                    add(qConfigEntity.status.eq(query.getStatus()));
                }
            }
        }), jpaConfig, entity -> mappingService.mapping(entity, ConfigDTO.class));
    }
}
