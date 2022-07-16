package ${basePackage}.api.base;

import top.zenyoung.common.vo.ResultVO;
import org.springframework.web.bind.annotation.*;

/**
 * API基类
 *
 * @author generator
 * date ${date?string("yyyy-MM-dd")}
 */
public interface BaseApi<DTO, VO> {
    /**
     * 公共新增方法
     * @param dto 新增请求数据
     * @return 新增结果
     */
    @PostMapping("/")
    ResultVO<VO> add(@RequestBody final DTO dto);

    /**
     * 公共根据ID查询对象
     * @param id 主键ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    ResultVO<VO> get(@PathVariable final ${idType} id);

    /**
     * 公共修改
     * @param id 主键ID
     * @param dto 修改数据
     * @return 修改结果
     */
    @PostMapping("/{id}")
    ResultVO<?> update(@PathVariable final ${idType} id, @RequestBody final DTO dto);

    /**
     * 公共删除
     * @param ids 主键ID
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    ResultVO<?> delete(@PathVariable final ${idType}[] ids);
}