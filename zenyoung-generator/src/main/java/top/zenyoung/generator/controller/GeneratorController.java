package top.zenyoung.generator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 代码生成器-控制器
 *
 * @author young
 */
@Controller
@RequestMapping("/gen")
public class GeneratorController {

    /**
     * 代码生成器-入口
     *
     * @return 入口页面
     */
    @GetMapping(value = {"", "/", "/index"})
    public String getIndex() {

        return "/generator";
    }
}
