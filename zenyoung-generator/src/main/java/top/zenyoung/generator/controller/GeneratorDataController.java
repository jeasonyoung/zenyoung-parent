package top.zenyoung.generator.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.zenyoung.generator.model.DatabaseConnect;

/**
 * 代码生成器-数据-控制器
 *
 * @author young
 */
@Slf4j
@RestController
@RequestMapping("/gen/data")
public class GeneratorDataController {

    private static class ConnectTestReq extends DatabaseConnect {
    }
}
