package top.zenyoung.code.generator.common;

import javax.annotation.Nonnull;
import java.io.OutputStream;

/**
 * 代码生成-服务接口
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/3/9 1:18 下午
 **/
public interface CodeGeneratorService {

    /**
     * 测试数据库连接字符串
     *
     * @param connection 数据库连接字符串
     * @throws Exception 测试异常
     */
    void testDatabase(@Nonnull final String connection) throws Exception;

    /**
     * 代码生成
     *
     * @param connection   数据库连接字符串
     * @param type         代码生成类型
     * @param outputStream 生成数据流
     * @throws Exception 生成异常
     */
    void generator(@Nonnull final String connection, @Nonnull final CodeType type, @Nonnull final OutputStream outputStream) throws Exception;
}
