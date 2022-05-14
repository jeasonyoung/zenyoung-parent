package top.zenyoung.framework.system.dao.repository.impl;

import com.google.common.base.Strings;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import top.zenyoung.common.paging.PagingResult;
import top.zenyoung.data.repository.impl.BaseRepositoryImpl;
import top.zenyoung.framework.system.dao.entity.LoginLogEntity;
import top.zenyoung.framework.system.dao.entity.QLoginLogEntity;
import top.zenyoung.framework.system.dao.jpa.JpaLoginLog;
import top.zenyoung.framework.system.dao.repository.LoginLogRepository;
import top.zenyoung.framework.system.dto.LoginLogAddDTO;
import top.zenyoung.framework.system.dto.LoginLogDTO;
import top.zenyoung.framework.system.dto.LoginLogQueryDTO;
import top.zenyoung.service.BeanMappingService;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.LinkedList;

/**
 * 登录日志-数据操作接口实现
 *
 * @author young
 */
@Repository
@RequiredArgsConstructor
public class LoginLogRepositoryImpl extends BaseRepositoryImpl implements LoginLogRepository {
    private final JPAQueryFactory queryFactory;
    private final JpaLoginLog jpaLoginLog;
    private final BeanMappingService mappingService;

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public PagingResult<LoginLogDTO> query(@Nonnull final LoginLogQueryDTO query) {
        return buildPagingQuery(query, q -> buildDslWhere(new LinkedList<BooleanExpression>() {
            {
                final QLoginLogEntity qLoginLogEntity = QLoginLogEntity.loginLogEntity;
                //用户账号
                final String account;
                if (!Strings.isNullOrEmpty(account = query.getAccount())) {
                    final String like = "%" + account + "%";
                    add(qLoginLogEntity.account.like(like));
                }
                //开始日期/结束日期
                if (query.getStart() != null && query.getEnd() != null) {
                    add(qLoginLogEntity.createTime.between(query.getStart(), query.getEnd()));
                }
            }
        }), jpaLoginLog, this::convert);
    }

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    @Override
    public LoginLogDTO getById(@Nonnull final Long id) {
        return convert(jpaLoginLog.getById(id));
    }

    private LoginLogDTO convert(final LoginLogEntity entity) {
        return mappingService.mapping(entity, LoginLogDTO.class);
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Long add(@Nonnull final LoginLogAddDTO data) {
        final LoginLogEntity entity = mappingService.mapping(data, LoginLogEntity.class);
        //保存数据
        return jpaLoginLog.save(entity).getId();
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Integer batchDels(@Nonnull final Date start, @Nonnull final Date end) {
        final QLoginLogEntity qLoginLogEntity = QLoginLogEntity.loginLogEntity;
        return (int) queryFactory.delete(qLoginLogEntity)
                .where(qLoginLogEntity.createTime.between(start, end))
                .execute();
    }
}
