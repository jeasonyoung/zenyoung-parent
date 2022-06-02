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
import top.zenyoung.framework.system.dao.repository.OperaLogRepository;
import top.zenyoung.framework.system.dto.OperaLogDTO;
import top.zenyoung.framework.system.dto.OperaLogDelDTO;
import top.zenyoung.framework.system.dto.OperaLogQueryDTO;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.web.vo.ResultVO;

/**
 * 操作日志-控制器
 *
 * @author young
 */
@RestController
@RequiredArgsConstructor
@ApiSupport(order = 19)
@RequestMapping("/monitor/opera")
@Api(value = "1.9-操作日志管理", tags = "1.系统管理")
public class OperaLogController extends BaseController {
    private final OperaLogRepository repository;

    /**
     * 操作日志-查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/query")
    @ApiOperationSupport(order = 1)
    @ApiOperation("1.9.1.操作日志-查询")
    @PreAuthorize("@ss.hasPermi('monitor:log-opera:query')")
    public ResultVO<DataResult<OperaLogDTO>> query(final OperaLogQueryDTO query) {
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
    @ApiOperation("1.9.2.操作日志-加载")
    @ApiImplicitParam(name = "id", value = "操作日志ID", paramType = "path", dataTypeClass = Long.class)
    public ResultVO<OperaLogDTO> getById(@PathVariable final Long id) {
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
    @ApiOperation("1.9.3.操作日志-批量删除")
    @PreAuthorize("@ss.hasPermi('monitor:log-opera:del')")
    public ResultVO<Void> batchDel(final OperaLogDelDTO dto) {
        final boolean ret = repository.batchDel(dto);
        return ret ? success() : failed();
    }
}
