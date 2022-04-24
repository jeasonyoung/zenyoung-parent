package top.zenyoung.framework.system.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.framework.system.dao.repository.ConfigRepository;
import top.zenyoung.framework.system.dto.ConfigDTO;
import top.zenyoung.framework.system.dto.ConfigQueryDTO;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.web.vo.ResultVO;

/**
 * 参数配置-控制器
 *
 * @author young
 */
@RestController
@Api("1.7-参数管理")
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class ConfigController extends BaseController {
    private final ConfigRepository repository;

    /**
     * 查询参数配置集合
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/query")
    @ApiOperation("1.7.1.参数管理-查询")
    @PreAuthorize("@ss.hasPermi('system:config:query')")
    public ResultVO<DataResult<ConfigDTO>> query(final ConfigQueryDTO query) {
        return success(repository.query(query));
    }
}
