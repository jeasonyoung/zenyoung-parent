package top.zenyoung.jfx.support;

import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The annotation {@link FXMLController} is used to mark JavaFX controller
 * classes. Usage of this annotation happens besides registration of such within
 * fxml descriptors.
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/1/23 5:47 下午
 */
@Component
@Retention(RetentionPolicy.RUNTIME)
public @interface FXMLController {

}
