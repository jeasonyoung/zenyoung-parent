package top.zenyoung.code.generator.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import top.zenyoung.webflux.BaseController;

/**
 * index-控制器
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/3/8 5:56 下午
 **/
@Slf4j
@Controller
@RequestMapping({"/", ""})
public class IndexController extends BaseController {

    @GetMapping
    public Mono<String> getIndex() {
        return Mono.create(sink -> sink.success("index"));
    }

}