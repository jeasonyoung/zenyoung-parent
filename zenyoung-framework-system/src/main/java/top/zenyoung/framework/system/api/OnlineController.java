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
import top.zenyoung.framework.system.dto.OnlineDTO;
import top.zenyoung.framework.system.dto.OnlineQueryDTO;
import top.zenyoung.framework.system.service.OnlineService;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.common.vo.ResultVO;

/**
 * 用户在线-控制器
 *
 * @author young
 */
@RestController
@RequiredArgsConstructor
@ApiSupport(order = 110)
@RequestMapping("/monitor/online")
@Api(value = "1.10-用户在线管理", tags = "1.10系统管理-用户在线管理")
public class OnlineController extends BaseController {
    private final OnlineService service;

    /**
     * 用户在线管理-分页查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/query")
    @ApiOperationSupport(order = 1)
    @ApiOperation("1.10.1.用户在线管理-查询")
    @PreAuthorize("@ss.hasPermi('monitor:online:query')")
    public ResultVO<DataResult<OnlineDTO>> query(final OnlineQueryDTO query) {
        return success(service.query(query));
    }

    /**
     * 用户在线管理-加载
     *
     * @param key 用户Key
     * @return 在线用户数据
     */
    @GetMapping("/{key}")
    @ApiOperationSupport(order = 2)
    @ApiOperation("1.10.2.用户在线管理-加载")
    @ApiImplicitParam(name = "key", value = "用户Key", paramType = "path", dataTypeClass = String.class)
    public ResultVO<OnlineDTO> getByKey(@PathVariable final String key) {
        return success(service.getByKey(key));
    }

    /**
     * 用户在线管理-单条强退
     *
     * @param key 用户Key
     * @return 退出结果
     */
    @DeleteMapping("/force/{key}")
    @ApiOperationSupport(order = 3)
    @ApiOperation("1.10.3.用户在线管理-单条强退")
    @PreAuthorize("@ss.hasPermi('monitor:online:force')")
    @ApiImplicitParam(name = "key", value = "用户Key", paramType = "path", dataTypeClass = String.class)
    public ResultVO<Void> forceDelByKey(@PathVariable final String key) {
        final String[] keys = {key};
        final boolean ret = service.batchForceExitByKeys(keys);
        return ret ? success() : failed();
    }

    /**
     * 用户在线管理-批量强退
     *
     * @param keys 用户Key集合
     * @return 退出结果
     */
    @DeleteMapping("/batch/{keys}")
    @ApiOperationSupport(order = 4)
    @ApiOperation("1.10.4.用户在线管理-批量强退")
    @PreAuthorize("@ss.hasPermi('monitor:online:batch')")
    @ApiImplicitParam(name = "keys", value = "用户Key集合", paramType = "path", dataTypeClass = String.class)
    public ResultVO<Void> forceDelByKey(@PathVariable final String[] keys) {
        final boolean ret = service.batchForceExitByKeys(keys);
        return ret ? success() : failed();
    }
}
