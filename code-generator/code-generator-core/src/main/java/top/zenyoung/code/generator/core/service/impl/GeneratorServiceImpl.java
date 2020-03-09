package top.zenyoung.code.generator.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import top.zenyoung.code.generator.common.CodeType;
import top.zenyoung.code.generator.core.service.GeneratorService;

import javax.annotation.Nonnull;
import java.io.OutputStream;

/**
 * 代码生成-服务接口实现
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/3/9 1:56 下午
 **/
@Slf4j
@Service
public class GeneratorServiceImpl implements GeneratorService {

    @Override
    public void testDatabase(@Nonnull final String connection) throws Exception {
        log.debug("testDatabase(connection: {})...", connection);
        Assert.hasText(connection, "'connection'不能为空!");

        ///TODO:
    }

    @Override
    public void generator(@Nonnull final String connection, @Nonnull final CodeType type, @Nonnull final OutputStream outputStream) throws Exception {
        log.debug("generator(connection: {},type: {})...", connection, type);
        //测试数据库连接
        testDatabase(connection);
        //

        ///TODO:

    }
}
