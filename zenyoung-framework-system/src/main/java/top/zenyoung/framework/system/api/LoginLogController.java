package top.zenyoung.framework.system.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.framework.system.dao.repository.LoginLogRepository;
import top.zenyoung.framework.system.dto.LoginLogDTO;
import top.zenyoung.framework.system.dto.LoginLogQueryDTO;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.web.vo.ResultVO;

/**
 * 登录日志-控制器
 *
 * @author young
 */
@RestController
@Api("1.6-登录日志管理")
@RequiredArgsConstructor
@RequestMapping("/system/log/login")
public class LoginLogController extends BaseController {
    private final LoginLogRepository repository;

    /**
     * 登录日志-查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/query")
    @ApiOperation("1.6.1.字典类型管理-查询")
    @PreAuthorize("@ss.hasPermi('system:log-login:query')")
    public ResultVO<DataResult<LoginLogDTO>> query(final LoginLogQueryDTO query) {
        return success(repository.query(query));
    }

    /**
     * 登录日志-加载数据
     *
     * @param id 登录日志ID
     * @return 加载数据
     */
    @GetMapping("/query")
    @ApiOperation("1.6.2.字典类型管理-加载")
    @PreAuthorize("@ss.hasPermi('system:log-login:Load')")
    public ResultVO<LoginLogDTO> getById(final Long id) {
        return success(repository.getById(id));
    }
}
