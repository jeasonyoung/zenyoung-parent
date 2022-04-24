package top.zenyoung.framework.system.api;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.zenyoung.web.controller.BaseController;

/**
 * 参数配置-控制器
 *
 * @author young
 */
@RestController
@Api("1.07-参数管理")
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class ConfigController extends BaseController {


}
