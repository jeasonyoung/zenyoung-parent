package top.zenyoung.framework.generator.api;

import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import top.zenyoung.common.vo.ResultVO;
import top.zenyoung.framework.generator.Generator;
import top.zenyoung.framework.generator.config.GeneratorAutoProperties;
import top.zenyoung.framework.generator.config.GeneratorProperties;
import top.zenyoung.framework.generator.dto.GeneratorDTO;
import top.zenyoung.framework.generator.ftl.FtlFileType;
import top.zenyoung.framework.generator.util.NameUtils;
import top.zenyoung.framework.generator.vo.FileVO;
import top.zenyoung.framework.generator.vo.TableVO;
import top.zenyoung.web.controller.BaseController;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 代码生成器-数据-控制器
 *
 * @author young
 */
@Slf4j
@RestController
@Api("代码生成接口")
@RequiredArgsConstructor
@RequestMapping("/codegen")
public class ApiController extends BaseController {
    private final GeneratorAutoProperties properties;
    private final Generator generator;


    /**
     * 获取是否独立部署
     *
     * @return 是否独立部署
     */
    @GetMapping("/get-alone")
    public ResultVO<Boolean> getAlone() {
        return success(properties.isAlone());
    }

    /**
     * 获取文件类型集合
     *
     * @return 文件类型集合
     */
    @GetMapping("/get-file-types")
    public ResultVO<List<String>> getFileTypes() {
        return success(Stream.of(FtlFileType.values())
                .map(Enum::name)
                .collect(Collectors.toList())
        );
    }

    /**
     * 获取数据库集合
     *
     * @return 数据库集合
     */
    @GetMapping("/get-databases")
    public ResultVO<List<String>> getDatabases() {
        try {
            final List<String> databases = generator.getDatabases();
            return success(databases);
        } catch (Throwable e) {
            log.error("getDatabases-exp: {}", e.getMessage());
            return failed(e.getMessage());
        }
    }

    /**
     * 获取数据库下的表集合
     *
     * @param dbName 数据库名
     * @return 表集合
     */
    @GetMapping("/{dbName}/tables")
    public ResultVO<List<TableVO>> getAllTables(@PathVariable final String dbName) {
        try {
            return success(generator.getTables(dbName));
        } catch (Throwable e) {
            log.error("getAllTables(dbName: {})-exp: {}", dbName, e.getMessage());
            return failed(e.getMessage());
        }
    }

    /**
     * 构建预览文件集合
     *
     * @param dto 请求数据
     * @return 响应数据
     */
    @PostMapping("/preview")
    public ResultVO<List<FileVO>> buildPreviewFiles(@RequestBody final GeneratorDTO dto) {
        try {
            return success(generator.build(merge(this.properties, dto)));
        } catch (Throwable e) {
            log.error("buildPreviewFiles(dto: {})-exp: {}", dto, e.getMessage());
            return failed(e.getMessage());
        }
    }

    /**
     * 构建下载数据
     *
     * @param dto dto
     * @param res 响应报文
     * @throws IOException IO异常
     */
    @PostMapping("/download")
    public void download(@RequestBody final GeneratorDTO dto, final HttpServletResponse res) throws IOException {
        final String contentDisposition = String.format("attachment;filename=%s", dto.getModuleName());
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
        res.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        generator.buildZip(merge(this.properties, dto), res.getOutputStream());
    }

    /**
     * 构建本地代码
     *
     * @param dto dto
     * @return 响应报文
     */
    @PostMapping("/local")
    public ResultVO<?> local(@RequestBody final GeneratorDTO dto) {
        try {
            final File target = new File(NameUtils.pathJoiner(".", "target", "generated-code"));
            if (!target.exists()) {
                final boolean ret = target.mkdirs();
                log.info("local-生成目录[{}]: {}", ret, target.getAbsolutePath());
            }
            //生成代码文件
            generator.buildFiles(merge(this.properties, dto), target);
            //返回数据
            return success();
        } catch (Throwable e) {
            log.error("local(dto: {})-exp: {}", dto, e.getMessage());
            return failed(e.getMessage());
        }
    }

    private static GeneratorDTO merge(@Nonnull final GeneratorProperties properties, @Nonnull final GeneratorDTO dto) {
        final GeneratorDTO data = new GeneratorDTO();
        BeanUtils.copyProperties(properties, data);
        //复制替换数据
        ReflectionUtils.doWithFields(GeneratorDTO.class, field -> {
            field.setAccessible(true);
            //获取对象值
            final Object val = field.get(dto);
            if (Objects.nonNull(val)) {
                if ((val instanceof String) && Strings.isNullOrEmpty((String) val)) {
                    return;
                }
                //设置值
                field.set(data, val);
            }
        });
        return data;
    }
}
