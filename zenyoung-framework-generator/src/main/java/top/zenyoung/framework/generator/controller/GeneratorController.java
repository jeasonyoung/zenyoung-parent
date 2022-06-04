package top.zenyoung.framework.generator.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.framework.generator.domain.Column;
import top.zenyoung.framework.generator.domain.Table;
import top.zenyoung.framework.generator.service.DatabaseConnectService;
import top.zenyoung.framework.generator.service.GeneratorCodeService;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.common.vo.ResultVO;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 代码生成器-数据-控制器
 *
 * @author young
 */
@Slf4j
@RestController
@Api("代码生成接口")
@RequestMapping("/gen")
@RequiredArgsConstructor
public class GeneratorController extends BaseController {
    private final DatabaseConnectService connectService;
    private final GeneratorCodeService codeService;

    /**
     * 测试数据库链接字符串
     *
     * @return 响应报文
     */
    @PostMapping("/test")
    @ApiOperation("测试数据库链接")
    public ResultVO<?> testConnect() {
        connectService.testDatabaseConnect();
        return success();
    }

    /**
     * 获取全部的表数据
     *
     * @param queryTableName 查询条件
     * @return 响应报文
     */
    @GetMapping("/tables")
    @ApiOperation("获取数据表")
    @ApiImplicitParam(name = "queryTableName", value = "表名称(支持模糊匹配)", paramType = "query")
    public ResultVO<DataResult<Table>> getTables(@RequestParam(required = false) final String queryTableName) {
        return success(DataResult.of(connectService.queryTables(queryTableName)));
    }

    /**
     * 获取预览数据
     *
     * @param tableName 表名
     * @return 响应数据
     */
    @GetMapping("/preview")
    @ApiOperation("获取预览数据")
    @ApiImplicitParam(name = "tableName", value = "表名称(完全匹配)", paramType = "query")
    public ResultVO<DataResult<PreviewBodyRes>> getPreview(final String tableName) {
        Assert.hasText(tableName, "'tableName'不能为空!");
        final Map<String, String> codes = buildGenCode(tableName);
        return success(DataResult.of(codes.entrySet().stream()
                .map(entry -> PreviewBodyRes.of(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList())
        ));
    }

    /**
     * 获取下载代码
     *
     * @param tableName 表名
     * @param response  响应数据
     */
    @GetMapping("/download")
    @ApiOperation("获取下载代码")
    @ApiImplicitParam(name = "tableName", value = "表名称(完全匹配)", paramType = "query")
    public void download(final String tableName, final HttpServletResponse response) throws IOException {
        Assert.hasText(tableName, "'tableName'不能为空!");
        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + tableName + ".zip\"");
        response.setContentType("application/octet-stream; charset=UTF-8");
        //
        final Map<String, String> codes = buildGenCode(tableName);
        codeService.buildZipStream(codes, response.getOutputStream());
    }

    private Map<String, String> buildGenCode(@Nonnull final String tableName) {
        if (!Strings.isNullOrEmpty(tableName)) {
            //表数据
            final Table table = connectService.getTable(tableName);
            if (table != null) {
                //查询表字段
                final List<Column> columns = connectService.getColumns(tableName);
                if (!CollectionUtils.isEmpty(columns)) {
                    return codeService.generatorCodes(table, columns);
                }
            }
        }
        return Maps.newHashMap();
    }

    @Data
    @AllArgsConstructor(staticName = "of")
    private static class PreviewBodyRes implements Serializable {
        /**
         * 文件名
         */
        private String fileName;
        /**
         * 文件内容
         */
        private String fileContent;
    }
}
