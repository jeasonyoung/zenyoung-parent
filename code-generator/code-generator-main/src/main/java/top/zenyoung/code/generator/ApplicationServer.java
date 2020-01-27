package top.zenyoung.code.generator;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
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
public class ApplicationServer extends Application implements CommandLineRunner {

    public static void main(final String[] args) {
        new SpringApplicationBuilder(ApplicationServer.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(final String... args) throws Exception {
        log.debug("启动JavaFx...");
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        final Text text = new Text(10, 40, "Hello World!");
        text.setFont(new Font(40));
        final Scene scene = new Scene(new Group(text));

        stage.setTitle("代码生成器");
        stage.setWidth(600);
        stage.setHeight(400);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }
}
