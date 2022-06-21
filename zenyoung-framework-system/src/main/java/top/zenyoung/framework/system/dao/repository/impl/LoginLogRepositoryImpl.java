package top.zenyoung.framework.system.dao.repository.impl;

import com.google.common.base.Strings;
import com.querydsl.core.types.dsl.BooleanExpression;
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
import top.zenyoung.framework.system.dto.LoginLogDelDTO;
import top.zenyoung.framework.system.dto.LoginLogQueryDTO;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.Objects;

/**
 * 登录日志-数据操作接口实现
 *
 * @author young
 */
@Repository
@RequiredArgsConstructor
public class LoginLogRepositoryImpl extends BaseRepositoryImpl implements LoginLogRepository {
    private final JpaLoginLog jpaLoginLog;

    @Override
    @Transactional(readOnly = true, rollbackFor = Throwable.class)
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
        }), jpaLoginLog, entity -> mapping(entity, LoginLogDTO.class));
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    public LoginLogDTO getById(@Nonnull final Long id) {
        return mapping(jpaLoginLog.getOne(id), LoginLogDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Long add(@Nonnull final LoginLogAddDTO data) {
        final LoginLogEntity entity = mapping(data, LoginLogEntity.class);
        //保存数据
        return jpaLoginLog.save(entity).getId();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean batchDels(@Nonnull final LoginLogDelDTO dto) {
        final QLoginLogEntity qEntity = QLoginLogEntity.loginLogEntity;
        return buildDslDeleteClause(queryFactory.delete(qEntity))
                //用户ID
                .add(Objects.nonNull(dto.getUserId()), qEntity.userId.eq(dto.getUserId()))
                //时间
                .add(Objects.nonNull(dto.getStart()) && Objects.nonNull(dto.getEnd()), qEntity.createTime.between(dto.getStart(), dto.getEnd()))
                .execute();
    }
}
