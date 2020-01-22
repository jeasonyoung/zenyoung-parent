package top.zenyoung.code.generator;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 代码生成器入口
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/1/18 4:39 下午
 **/
@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApplicationServer implements CommandLineRunner {

    public static void main(final String[] args) {
        new SpringApplicationBuilder(ApplicationServer.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(final String... args) {
        log.info("启动桌面...");
        Application.launch(AppMain.class, args);
    }

    public static class AppMain extends Application {

        @Override
        public void start(final Stage stage) {
            stage.setTitle("JavaFx");
            //设置窗体框高
            stage.setWidth(1200);
            stage.setHeight(600);
            //设置窗口模式
            stage.initStyle(StageStyle.DECORATED);
            //显示窗口
            stage.show();
        }
    }
}
