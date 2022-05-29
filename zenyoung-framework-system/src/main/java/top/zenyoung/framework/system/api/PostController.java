package top.zenyoung.framework.system.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.zenyoung.common.paging.DataResult;
import top.zenyoung.common.valid.Insert;
import top.zenyoung.common.valid.Modify;
import top.zenyoung.framework.system.dao.repository.PostRepository;
import top.zenyoung.framework.system.dto.PostAddDTO;
import top.zenyoung.framework.system.dto.PostDTO;
import top.zenyoung.framework.system.dto.PostModifyDTO;
import top.zenyoung.framework.system.dto.PostQueryDTO;
import top.zenyoung.web.controller.BaseController;
import top.zenyoung.web.vo.ResultVO;

/**
 * 岗位管理-控制器
 *
 * @author young
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/sys/post")
@Api(value = "1.2-岗位管理", tags = "1.系统管理")
public class PostController extends BaseController {
    private final PostRepository repository;

    /**
     * 岗位管理-查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/query")
    @ApiOperation("1.2.1.岗位管理-查询")
    @PreAuthorize("@ss.hasPermi('sys:post:query')")
    public ResultVO<DataResult<PostDTO>> query(final PostQueryDTO query) {
        return success(repository.query(query));
    }

    /**
     * 岗位管理-加载
     *
     * @param id 岗位ID
     * @return 岗位数据
     */
    @GetMapping("/{id}")
    @ApiOperation("1.2.2.岗位管理-加载")
    @ApiImplicitParam(name = "id", value = "岗位ID", paramType = "path", dataTypeClass = Long.class)
    public ResultVO<PostDTO> getById(@PathVariable final Long id) {
        return success(repository.getById(id));
    }

    /**
     * 岗位管理-新增
     *
     * @param data 新增数据
     * @return 新增结果
     */
    @PostMapping
    @ApiOperation("1.2.3.岗位管理-新增")
    @PreAuthorize("@ss.hasPermi('sys:post:add')")
    public ResultVO<Long> add(@RequestBody @Validated({Insert.class}) final PostAddDTO data) {
        return success(repository.add(data));
    }

    /**
     * 岗位管理-修改
     *
     * @param id   岗位ID
     * @param data 修改数据
     * @return 修改结果
     */
    @PutMapping("/{id}")
    @ApiOperation("1.2.4.岗位管理-修改")
    @PreAuthorize("@ss.hasPermi('sys:post:edit')")
    @ApiImplicitParam(name = "id", value = "岗位ID", paramType = "path", dataTypeClass = Long.class)
    public ResultVO<Void> modify(@PathVariable final Long id,
                                 @RequestBody @Validated({Modify.class}) final PostModifyDTO data) {
        final boolean ret = repository.update(id, data);
        return ret ? success() : failed();
    }

    /**
     * 岗位管理-删除
     *
     * @param ids 岗位ID集合
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @ApiOperation("1.2.5.岗位管理-删除")
    @PreAuthorize("@ss.hasPermi('sys:post:del')")
    @ApiImplicitParam(name = "ids", value = "岗位ID集合", paramType = "path", dataTypeClass = Long[].class)
    public ResultVO<Void> del(@PathVariable final Long[] ids) {
        final boolean ret = repository.delByIds(ids);
        return ret ? success() : failed();
    }
}
