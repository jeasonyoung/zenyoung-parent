package top.zenyoung.common.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import freemarker.cache.ByteArrayTemplateLoader;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * FreeMaker工具类
 *
 * @author young
 */
@Slf4j
public class FtlUtils {
    private final ByteArrayTemplateLoader dynamicLoader;
    private final Configuration config;

    private FtlUtils(@Nullable final Class<?> resourceLoaderClass, @Nullable final String basePackagePath) {
        log.debug("FtlUtils(resourceLoaderClass: {},basePackagePath: {})...", resourceLoaderClass, basePackagePath);
        //初始化配置
        this.config = new Configuration(Configuration.VERSION_2_3_30);
        //动态模板加载器
        this.dynamicLoader = new ByteArrayTemplateLoader();
        //模板列表
        final List<TemplateLoader> loaders = Lists.newArrayList(this.dynamicLoader);
        //添加静态模版
        if (Objects.nonNull(resourceLoaderClass) && !Strings.isNullOrEmpty(basePackagePath)) {
            final TemplateLoader staticLoader = new ClassTemplateLoader(resourceLoaderClass, basePackagePath);
            loaders.add(staticLoader);
        }
        //设置模板加载器
        this.config.setTemplateLoader(new MultiTemplateLoader(loaders.toArray(new TemplateLoader[0])));
        //设置字符集
        this.config.setDefaultEncoding(StandardCharsets.UTF_8.name());
    }

    /**
     * 获取模板工具实例
     *
     * @param resourceLoaderClass 模板资源加载器
     * @param basePackagePath     模板资源路径
     * @return 模板工具
     */
    public static FtlUtils getInstance(@Nullable final Class<?> resourceLoaderClass, @Nullable final String basePackagePath) {
        return new FtlUtils(resourceLoaderClass, basePackagePath);
    }

    /**
     * 获取模板工具实例
     *
     * @param resourceLoaderClass 模板资源加载器
     * @return 模板工具
     */
    public static FtlUtils getInstance(@Nonnull final Class<?> resourceLoaderClass) {
        return getInstance(resourceLoaderClass, "/templates");
    }

    /**
     * 获取模板工具实例
     *
     * @return 模板工具
     */
    public static FtlUtils getInstance() {
        return getInstance(FtlUtils.class, "/templates");
    }

    /**
     * 生成模板文件内容
     *
     * @param templateName 模板文件名
     * @param params       模板参数数据
     * @param out          目标内容
     */
    public void process(@Nonnull final String templateName, @Nonnull final Map<String, Object> params, @Nonnull final Writer out) {
        if (!Strings.isNullOrEmpty(templateName)) {
            final long start = System.currentTimeMillis();
            try {
                final Template template = this.config.getTemplate(templateName);
                template.process(params, out);
            } catch (TemplateException | IOException e) {
                log.error("process(templateName: {})-exp: {}", templateName, e.getMessage());
            } finally {
                log.info("生成模板[{}]耗时: {}ms", templateName, (System.currentTimeMillis() - start));
            }
        }
    }

    /**
     * 生成模板文件内容
     *
     * @param templateName 模板文件名
     * @param params       模板参数数据
     * @return 文件内容
     */
    public String process(@Nonnull final String templateName, @Nonnull final Map<String, Object> params) {
        try (final StringWriter writer = new StringWriter()) {
            process(templateName, params, writer);
            writer.flush();
            return writer.toString();
        } catch (IOException e) {
            log.warn("process(templateName: {},params: {})-exp: {}", templateName, params, e.getMessage());
        }
        return "";
    }

    /**
     * 根据动态模板生成内容
     *
     * @param content 模板内容
     * @param params  模板参数数据
     * @param out     生成后的内容
     */
    public void dynamicProcess(@Nonnull final String content, @Nonnull final Map<String, Object> params, @Nonnull final Writer out) {
        if (!Strings.isNullOrEmpty(content)) {
            try {
                final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
                final String name = String.format("tmp-%s.ftl", DigestUtils.md5Hex(bytes));
                this.dynamicLoader.putTemplate(name, bytes);
                process(name, params, out);
            } catch (NullPointerException e) {
                log.error("dynamicProcess(content: {},params: {})-exp: {}", content, params, e.getMessage());
            }
        }
    }

    /**
     * 根据动态模板生成内容
     *
     * @param content 模板内容
     * @param params  模板参数数据
     * @return 生成后的内容
     */
    public String dynamicProcess(@Nonnull final String content, @Nonnull final Map<String, Object> params) {
        try (final StringWriter writer = new StringWriter()) {
            dynamicProcess(content, params, writer);
            writer.flush();
            return writer.toString();
        } catch (IOException e) {
            log.warn("dynamicProcess(content: {},params: {})-exp: {}", content, params, e.getMessage());
        }
        return "";
    }

}
