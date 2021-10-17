package top.zenyoung.generator.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import top.zenyoung.generator.domain.Column;
import top.zenyoung.generator.domain.Table;
import top.zenyoung.generator.exceptions.AccessTokenException;
import top.zenyoung.generator.model.DatabaseConnect;
import top.zenyoung.generator.service.DatabaseConnectService;
import top.zenyoung.generator.service.GeneratorCacheService;
import top.zenyoung.generator.service.GeneratorCodeService;
import top.zenyoung.web.ExceptHandler;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.web.vo.RespDataResult;
import top.zenyoung.web.vo.RespResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 代码生成器-数据-控制器
 *
 * @author young
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/gen/data")
@Api(tags = {"代码生成器数据接口"})
public class GeneratorDataController extends BaseController {
    private final GeneratorCacheService cacheService;
    private final DatabaseConnectService connectService;
    private final GeneratorCodeService codeService;

    @Nonnull
    @Override
    protected List<ExceptHandler> getExceptHandlers() {
        return Lists.newArrayList(ExceptHandler.of(401, AccessTokenException.class));
    }

    private <R> R validToken(@Nullable final String token, @Nonnull final Supplier<R> handler) {
        Assert.hasText(token, "'token'不能为空!");
        //验证访问令牌
        if (!cacheService.verifyToken(token)) {
            throw new AccessTokenException();
        }
        //执行业务
        return handler.get();
    }

    private RespResult<Boolean> actionHandler(@Nullable final String token, @Nullable final ConnectReq reqBody, @Nonnull final Function<ConnectReq, Boolean> handler) {
        return action(reqBody, req -> validToken(token, () -> handler.apply(req)));
    }

    /**
     * 测试数据库链接字符串
     *
     * @param token   访问令牌
     * @param reqBody 请求报文
     * @return 响应报文
     */
    @PostMapping("/test")
    @ApiOperation(value = "测试数据库链接字符串")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "访问令牌", required = true, paramType = "header")})
    public RespResult<Boolean> testConnect(@RequestHeader("token") final String token, @RequestBody final ConnectReq reqBody) {
        return actionHandler(token, reqBody, req -> {
            //测试数据库链接
            connectService.testDatabaseConnect(req);
            //测试通过
            return true;
        });
    }

    /**
     * 保存数据库连接字符串
     *
     * @param token   访问令牌
     * @param reqBody 请求报文
     * @return 响应报文
     */
    @PostMapping("/save")
    @ApiOperation(value = "保存数据库连接字符串")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "访问令牌", required = true, paramType = "header")})
    public RespResult<Boolean> saveConnect(@RequestHeader("token") final String token, @RequestBody final ConnectReq reqBody) {
        return actionHandler(token, reqBody, req -> {
            //测试数据库连接
            connectService.testDatabaseConnect(reqBody);
            //保存数据库连接
            cacheService.putConnect(token, reqBody);
            //保存成功
            return true;
        });
    }

    /**
     * 获取全部的表数据
     *
     * @param token          访问令牌
     * @param queryTableName 查询条件
     * @return 响应报文
     */
    @GetMapping("/tables")
    @ApiOperation(value = "获取全部的表数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "访问令牌", required = true, paramType = "header"),
            @ApiImplicitParam(name = "queryTableName", value = "表名称(支持模糊匹配)", paramType = "query")
    })
    public RespDataResult<Table> getTables(@RequestHeader("token") final String token, @RequestParam(value = "queryTableName", required = false) final String queryTableName) {
        return buildQuery(() -> validToken(token, () -> {
            //获取连接数据
            final DatabaseConnect connect = cacheService.getConnect(token);
            if (connect == null) {
                throw new RuntimeException("数据库连接缓存已过期!");
            }
            return connectService.queryTables(connect, queryTableName);
        }), row -> row);
    }

    /**
     * 获取预览数据
     *
     * @param token     访问令牌
     * @param tableName 表名
     * @return 响应数据
     */
    @GetMapping("/preview")
    @ApiOperation(value = "获取预览数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "访问令牌", required = true, paramType = "header"),
            @ApiImplicitParam(name = "tableName", value = "表名称(完全匹配)", paramType = "query")
    })
    public RespDataResult<PreviewBodyResp> getPreview(@RequestHeader("token") final String token, @RequestParam("tableName") final String tableName) {
        return buildQuery(() -> {
            Assert.hasText(tableName, "'tableName'不能为空!");
            return validToken(token, () -> {
                final List<PreviewBodyResp> rows = Lists.newLinkedList();
                final Map<String, String> codes = buildGenCode(token, tableName);
                if (!CollectionUtils.isEmpty(codes)) {
                    codes.forEach((k, v) -> rows.add(PreviewBodyResp.of(k, v)));
                }
                return rows;
            });
        }, row -> row);
    }

    /**
     * 获取下载代码
     *
     * @param token     访问令牌
     * @param tableName 表名
     * @param response  响应数据
     */
    @GetMapping("/download")
    @ApiOperation(value = "获取下载代码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "访问令牌", required = true, paramType = "header"),
            @ApiImplicitParam(name = "tableName", value = "表名称(完全匹配)", paramType = "query")
    })
    public void download(@RequestHeader("token") final String token, @RequestParam("tableName") final String tableName, final HttpServletResponse response) {
        validToken(token, () -> {
            try {
                Assert.hasText(tableName, "'tableName'不能为空!");
                response.reset();
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + tableName + ".zip\"");
                //response.addHeader("Content-Length", "" + data.length);
                response.setContentType("application/octet-stream; charset=UTF-8");
                //
                final Map<String, String> codes = buildGenCode(token, tableName);
                codeService.buildZipStream(codes, response.getOutputStream());
            } catch (Throwable ex) {
                log.error("download(token: {},tableName: {})-exp: {}", token, tableName, ex.getMessage());
                throw new RuntimeException(ex);
            }
            return null;
        });
    }

    private Map<String, String> buildGenCode(@Nonnull final String token, @Nonnull final String tableName) {
        if (!Strings.isNullOrEmpty(token) && !Strings.isNullOrEmpty(tableName)) {
            //获取连接数据
            final DatabaseConnect connect = cacheService.getConnect(token);
            if (connect == null) {
                throw new RuntimeException("数据库连接缓存已过期!");
            }
            //表数据
            final Table table = connectService.getTable(connect, tableName);
            if (table != null) {
                //查询表字段
                final List<Column> columns = connectService.getColumns(connect, tableName);
                if (!CollectionUtils.isEmpty(columns)) {
                    return codeService.generatorCodes(table, columns);
                }
            }
        }
        return null;
    }


    private static class ConnectReq extends DatabaseConnect {
    }

    @Data
    @AllArgsConstructor(staticName = "of")
    private static class PreviewBodyResp implements Serializable {
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
