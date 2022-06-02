package top.zenyoung.framework.system.api;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.framework.system.dao.repository.LoginLogRepository;
import top.zenyoung.framework.system.dto.LoginLogDTO;
import top.zenyoung.framework.system.dto.LoginLogDelDTO;
import top.zenyoung.framework.system.dto.LoginLogQueryDTO;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.web.vo.ResultVO;

/**
 * 登录日志-控制器
 *
 * @author young
 */
@RestController
@RequiredArgsConstructor
@ApiSupport(order = 18)
@RequestMapping("/monitor/log-login")
@Api(value = "1.8-登录日志管理", tags = "1.系统管理-登录日志管理")
public class LoginLogController extends BaseController {
    private final LoginLogRepository repository;

    /**
     * 登录日志-查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/query")
    @ApiOperationSupport(order = 1)
    @ApiOperation("1.8.1.登录日志管理-查询")
    @PreAuthorize("@ss.hasPermi('monitor:log-login:query')")
    public ResultVO<DataResult<LoginLogDTO>> query(final LoginLogQueryDTO query) {
        return success(repository.query(query));
    }

    /**
     * 登录日志-加载数据
     *
     * @param id 登录日志ID
     * @return 加载数据
     */
    @GetMapping("/{id}")
    @ApiOperationSupport(order = 2)
    @ApiOperation("1.8.2.登录日志管理-加载")
    @ApiImplicitParam(name = "id", value = "登录日志ID", paramType = "path", dataTypeClass = Long.class)
    public ResultVO<LoginLogDTO> getById(@PathVariable final Long id) {
        return success(repository.getById(id));
    }

    /**
     * 登录日志-批量删除
     *
     * @param dto 删除条件
     * @return 删除结果
     */
    @DeleteMapping
    @ApiOperationSupport(order = 3)
    @ApiOperation("1.8.3.登录日志管理-批量删除")
    @PreAuthorize("@ss.hasPermi('monitor:log-login:del')")
    public ResultVO<Void> batchDel(final LoginLogDelDTO dto) {
        final boolean ret = repository.batchDels(dto);
        return ret ? success() : failed();
    }
}
