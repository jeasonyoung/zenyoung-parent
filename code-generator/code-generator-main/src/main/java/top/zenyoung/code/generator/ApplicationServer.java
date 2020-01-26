package top.zenyoung.code.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import top.zenyoung.code.generator.view.MainStageView;
import top.zenyoung.jfx.support.AbstractJavaFxApplicationSupport;

/**
 * 代码生成器入口
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/1/18 4:39 下午
 **/
@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApplicationServer extends AbstractJavaFxApplicationSupport {

    public static void main(final String[] args) {
        launch(ApplicationServer.class, MainStageView.class, args);
    }
}
