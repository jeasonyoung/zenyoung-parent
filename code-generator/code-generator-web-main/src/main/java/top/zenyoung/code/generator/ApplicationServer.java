package top.zenyoung.code.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * App Web 入口
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/3/8 3:23 下午
 **/
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApplicationServer {

    public static void main(final String[] args) {
        SpringApplication.run(ApplicationServer.class, args);
    }
}
