package top.zenyoung.framework.system.dao.repository.impl;

import com.google.common.base.Strings;
import com.querydsl.core.types.dsl.BooleanExpression;
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
import top.zenyoung.framework.system.dto.OperaLogDelDTO;
import top.zenyoung.framework.system.dto.OperaLogQueryDTO;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.Objects;

/**
 * 操作记录-数据服务接口实现
 *
 * @author young
 */
@Repository
@RequiredArgsConstructor
public class OperaLogRepositoryImpl extends BaseRepositoryImpl implements OperaLogRepository {
    private final JpaOperaLog jpaOperaLog;

    @Override
    @Transactional(readOnly = true, rollbackFor = Throwable.class)
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

    @Override
    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    public OperaLogDTO getById(@Nonnull final Long id) {
        return convert(jpaOperaLog.getOne(id));
    }

    private OperaLogDTO convert(final OperaLogEntity entity) {
        return mapping(entity, OperaLogDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Long add(@Nonnull final OperaLogAddDTO data) {
        final OperaLogEntity entity = mapping(data, OperaLogEntity.class);
        //保存数据
        return jpaOperaLog.save(entity).getId();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean batchDel(@Nonnull final OperaLogDelDTO dto) {
        final QOperaLogEntity qEntity = QOperaLogEntity.operaLogEntity;
        return buildDslDeleteClause(queryFactory.delete(qEntity))
                .add(!Strings.isNullOrEmpty(dto.getMethod()), qEntity.method.eq(dto.getMethod()))
                .add(Objects.nonNull(dto.getStart()) && Objects.nonNull(dto.getEnd()), qEntity.createTime.between(dto.getStart(), dto.getEnd()))
                .execute();
    }
}