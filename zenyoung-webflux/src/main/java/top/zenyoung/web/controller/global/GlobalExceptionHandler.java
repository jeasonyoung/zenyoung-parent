package top.zenyoung.web.controller.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.zenyoung.web.BaseExceptionController;

/**
 * 全局异常处理
 *
 * @author young
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionController {

}
