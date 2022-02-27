package top.zenyoung.framework.system.dao.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.zenyoung.data.repository.impl.BaseRepositoryImpl;
import top.zenyoung.framework.system.dao.dto.DeptAddDTO;
import top.zenyoung.framework.system.dao.dto.DeptLoadDTO;
import top.zenyoung.framework.system.dao.dto.DeptModifyDTO;
import top.zenyoung.framework.system.dao.repository.DeptRepository;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 部门-数据操作接口实现
 *
 * @author young
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DeptRepositoryImpl extends BaseRepositoryImpl implements DeptRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<DeptLoadDTO> getAllDepts() {
        return null;
    }

    @Override
    public DeptLoadDTO getDept(@Nonnull final Long id) {
        return null;
    }

    @Override
    public Long addDept(@Nonnull final DeptAddDTO data) {
        return null;
    }

    @Override
    public void modifyDept(@Nonnull final DeptModifyDTO data) {

    }

    @Override
    public int delDeptByIds(@Nonnull final List<Long> ids) {
        return 0;
    }
}
