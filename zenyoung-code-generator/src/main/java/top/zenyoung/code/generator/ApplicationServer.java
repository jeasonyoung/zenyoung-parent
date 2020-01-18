package top.zenyoung.code.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 代码生成器入口
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/1/18 4:39 下午
 **/
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApplicationServer {

    public static void main(final String[] args) {
        SpringApplication.run(ApplicationServer.class, args);
    }
}
