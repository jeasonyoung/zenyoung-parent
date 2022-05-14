package top.zenyoung.framework.system.dao.repository.impl;

import com.google.common.base.Strings;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.data.repository.impl.BaseRepositoryImpl;
import top.zenyoung.framework.system.dao.entity.OperaLogEntity;
import top.zenyoung.framework.system.dao.entity.QOperaLogEntity;
import top.zenyoung.framework.system.dao.jpa.JpaOperaLog;
import top.zenyoung.framework.system.dao.repository.OperaLogRepository;
import top.zenyoung.framework.system.dto.OperaLogAddDTO;
import top.zenyoung.framework.system.dto.OperaLogDTO;
import top.zenyoung.framework.system.dto.OperaLogQueryDTO;
import top.zenyoung.service.BeanMappingService;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.LinkedList;

/**
 * 操作记录-数据服务接口实现
 *
 * @author young
 */
@Repository
@RequiredArgsConstructor
public class OperaLogRepositoryImpl extends BaseRepositoryImpl implements OperaLogRepository {
    private final JPAQueryFactory queryFactory;
    private final JpaOperaLog jpaOperaLog;
    private final BeanMappingService mappingService;

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public PagingResult<OperaLogDTO> query(@Nonnull final OperaLogQueryDTO query) {
        return buildPagingQuery(query, q -> buildDslWhere(new LinkedList<BooleanExpression>() {
            {
                final QOperaLogEntity qOperaLogEntity = QOperaLogEntity.operaLogEntity;
                //模块标题/方法名称
                final String title;
                if (!Strings.isNullOrEmpty(title = query.getTitle())) {
                    final String like = "%" + title + "%";
                    add(qOperaLogEntity.title.like(like).or(qOperaLogEntity.method.like(like)));
                }
                //开始日期/结束日期
                if (query.getStart() != null && query.getEnd() != null) {
                    add(qOperaLogEntity.createTime.between(query.getStart(), query.getEnd()));
                }
            }
        }), jpaOperaLog, this::convert);
    }

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public OperaLogDTO getById(@Nonnull final Long id) {
        return convert(jpaOperaLog.getById(id));
    }

    private OperaLogDTO convert(final OperaLogEntity entity) {
        return mappingService.mapping(entity, OperaLogDTO.class);
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Long add(@Nonnull final OperaLogAddDTO data) {
        final OperaLogEntity entity = mappingService.mapping(data, OperaLogEntity.class);
        //保存数据
        return jpaOperaLog.save(entity).getId();
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Integer batchDel(@Nonnull final Date start, @Nonnull final Date end) {
        final QOperaLogEntity qOperaLogEntity = QOperaLogEntity.operaLogEntity;
        return (int) queryFactory.delete(qOperaLogEntity)
                .where(qOperaLogEntity.createTime.between(start, end))
                .execute();
    }
}
