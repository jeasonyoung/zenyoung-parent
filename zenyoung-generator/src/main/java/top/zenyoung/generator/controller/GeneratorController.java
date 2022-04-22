package top.zenyoung.generator.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import top.zenyoung.generator.service.GeneratorCacheService;
import top.zenyoung.web.controller.BaseController;

/**
 * 代码生成器-控制器
 *
 * @author young
 */
@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GeneratorController extends BaseController {
    private final GeneratorCacheService cacheService;

    /**
     * 代码生成器-入口
     *
     * @return 入口页面
     */
    @GetMapping(value = {"", "/", "/index"})
    public String getIndex() {
        return "redirect:/static/index.html#" + cacheService.createToken();
    }
}
