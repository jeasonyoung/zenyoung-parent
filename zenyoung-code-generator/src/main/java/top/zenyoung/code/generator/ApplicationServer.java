package top.zenyoung.code.generator;

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
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ApplicationServer {

    public static void main(final String[] args) {
        new SpringApplicationBuilder(ApplicationServer.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
