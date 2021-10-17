package ${packageName}.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.BeanUtils;
import top.zenyoung.common.model.UserPrincipal;

import top.zenyoung.web.controller.BaseController;
import top.zenyoung.web.valid.Insert;
import top.zenyoung.web.valid.Modify;
import top.zenyoung.web.vo.*;

import javax.annotation.Nonnull;

/**
 * ${comment!}-控制器
 * <#assign lastTime = .now>
 * @author ${author!}
 * @version 1.0
 * @date ${lastTime?iso_utc}
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/${moduleName!}")
public class ${className}Controller extends BaseController {
    private final ${className}Repository ${lowerClassName}Repository;

    /**
     * ${comment!}_列表
     *
     * @param principal 当前用户
     * @return 响应报文
     */
    @GetMapping("/query")
    public RespDataResult<${className}Res> query(@AuthenticationPrincipal final UserPrincipal principal) {
        final PagingQuery<${className}Req> pagingQuery = ReqUtils.parsePagingQuery(${className}Req.class, this);
        return buildAuthPagingQuery(principal, pagingQuery, (auth, req) -> {
            final ${className}DTO query = new ${className}DTO();
            if (req != null) {
                ///TODO: 查询条件转换
            }
            return query;
        }, ${lowerClassName}Repository::query, data -> {
            final ${className}Res resp = new ${className}Res();
            BeanUtils.copyProperties(data, resp);
            return resp;
        });
    }

    /**
     * ${comment!}_加载
     *
     * @param id      主键ID
     * @param principal 当前用户
     * @return 响应报文
     */
    @GetMapping("/{id}")
    public RespResult<${className}Res> get(@PathVariable final Long id, @AuthenticationPrincipal final UserPrincipal principal) {
        return actionAuth(principal, (auth, aVod) -> {
            Assert.isTrue(id != null && id > 0, "'id'不能为空!");
            final {className}DTO data = ${lowerClassName}Repository.loadById(adId);
            if (data != null) {
                final ${className}Res resp = new ${className}Res();
                BeanUtils.copyProperties(data, resp);
                return resp;
            }
            return null;
        });
    }

    /**
     * ${comment!}_新增
     *
     * @param addReq    请求报文
     * @param principal 当前用户
     * @return 响应报文
     */
    @PostMapping
    public RespAddResult add(@Validated({Insert.class}) @RequestBody final ${className}Req addReq, @AuthenticationPrincipal final UserPrincipal principal) {
        return actionAuthAdd(principal, addReq, (auth, req) -> {
            final ${className}DTO data = new ${className}DTO();
            BeanUtils.copyProperties(req, data);
            return ${lowerClassName}Repository.add(data);
        });
    }

    /**
     * ${comment!}_修改
     *
     * @param id        主键ID
     * @param modifyReq 请求报文
     * @param principal 当前用户
     * @return 响应报文
     */
    @PutMapping("/{id}")
    public RespModifyResult modify(@PathVariable final Long id, @Validated({Modify.class}) @RequestBody final ${className}Req modifyReq, @AuthenticationPrincipal final UserPrincipal principal) {
        return actionAuthModify(principal, modifyReq, (auth, req) -> {
            Assert.isTrue(id != null && id > 0, "'id'不能为空!");
            final ${className}DTO data = new ${className}DTO();
            BeanUtils.copyProperties(req, data);
            data.setId(id);
            ${lowerClassName}Repository.modify(data);
        });
    }

    /**
     * ${comment!}_删除
     *
     * @param id        主键ID
     * @param principal 当前用户
     * @return 响应报文
     */
    @DeleteMapping("/{id}")
    public RespDeleteResult delById(@PathVariable final Long id, @AuthenticationPrincipal final UserPrincipal principal) {
        return actionAuthDelete(principal, (auth, aVod) -> {
            Assert.isTrue(id != null && id > 0, "'id'不能为空!");
            ${lowerClassName}Repository.delById(id);
        });
    }
}