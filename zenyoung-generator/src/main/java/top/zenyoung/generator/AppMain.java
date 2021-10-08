package top.zenyoung.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * App入口
 *
 * @author young
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AppMain {

    public static void main(final String[] args) {
        SpringApplication.run(AppMain.class, args);
    }
}
