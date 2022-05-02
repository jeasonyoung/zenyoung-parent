package top.zenyoung.framework.system.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.framework.system.dao.repository.OperaLogRepository;
import top.zenyoung.framework.system.dto.OperaLogDTO;
import top.zenyoung.framework.system.dto.OperaLogQueryDTO;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.web.vo.ResultVO;

/**
 * 操作日志-控制器
 *
 * @author young
 */
@RestController
@Api("1.7-操作日志管理")
@RequiredArgsConstructor
@RequestMapping("/system/log/opera")
public class OperaLogController extends BaseController {
    private final OperaLogRepository repository;

    /**
     * 操作日志-查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/query")
    @ApiOperation("1.7.1.操作日志-查询")
    @PreAuthorize("@ss.hasPermi('system:log-opera:query')")
    public ResultVO<DataResult<OperaLogDTO>> query(final OperaLogQueryDTO query) {
        return success(repository.query(query));
    }

    /**
     * 登录日志-加载数据
     *
     * @param id 登录日志ID
     * @return 加载数据
     */
    @GetMapping("/query")
    @ApiOperation("1.7.2.操作日志-加载")
    @PreAuthorize("@ss.hasPermi('system:log-opera:Load')")
    public ResultVO<OperaLogDTO> getById(final Long id) {
        return success(repository.getById(id));
    }
}
