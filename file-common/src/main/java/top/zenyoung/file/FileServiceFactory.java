package top.zenyoung.file;

import javax.annotation.Nonnull;

/**
 * 文件工厂接口
 *
 * @author young
 */
public interface FileServiceFactory {
    /**
     * 获取类型
     *
     * @return 类型
     */
    @Nonnull
    String getType();

    /**
     * 创建文件服务
     *
     * @param prop 文件属性
     * @return 文件服务
     */
    FileService create(@Nonnull final FileProperties prop);
}
