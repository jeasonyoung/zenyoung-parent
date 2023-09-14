package top.zenyoung.jfx;

import com.google.common.base.Strings;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import top.zenyoung.common.util.ThreadUtils;
import top.zenyoung.jfx.util.JfxUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;

/**
 * jfx-控制器基类
 */
public abstract class BaseFxmlController implements ApplicationContextAware, Initializable {
    private static final Executor POOLS = ThreadUtils.createPools();
    private ApplicationContext context;
    private ResourceBundle resourceBundle;

    @Getter(AccessLevel.PROTECTED)
    private Node root;

    @Override
    public void setApplicationContext(@Nonnull final ApplicationContext ctx) throws BeansException {
        this.context = ctx;
    }

    /**
     * 设置i18n资源文件对象
     *
     * @param bundle i18n资源文件对象
     */
    public final void setResourceBundle(final ResourceBundle bundle) {
        this.resourceBundle = bundle;
    }

    /**
     * 根据资源键加载多语言值
     *
     * @param key 多语言键
     * @return 多语言值
     */
    protected final String getResourceBundleByKey(@Nonnull final String key) {
        return Optional.ofNullable(resourceBundle)
                .filter(res -> !Strings.isNullOrEmpty(key))
                .map(res -> res.getString(key))
                .orElse(null);
    }

    /**
     * 设置根节点
     *
     * @param root 根节点
     */
    public final void setRoot(@Nonnull final Node root) {
        this.root = root;
    }

    /**
     * 设置焦点
     */
    public void requestFocus() {
        Optional.ofNullable(root)
                .ifPresent(Node::requestFocus);
    }

    /**
     * 获取Bean对象
     *
     * @param beanClass Bean类型
     * @param <T>       Bean类型
     * @return Bean对象
     */
    protected final <T> T getBean(@Nonnull final Class<T> beanClass) {
        return Optional.ofNullable(context)
                .map(c -> c.getBean(beanClass))
                .orElse(null);
    }

    /**
     * 启动异步子线程执行
     *
     * @param handler 执行业务
     */
    protected void startAsyncRun(@Nonnull final Runnable handler) {
        POOLS.execute(handler);
    }

    /**
     * 启动异步任务执行
     *
     * @param asyncTask 异步任务
     * @param <T>       任务类型
     */
    protected <T extends Task<?>> void startAsyncTask(@Nonnull final T asyncTask) {
        startAsyncTask(asyncTask, null);
    }

    /**
     * 启动异步任务执行
     *
     * @param asyncTask      异步任务
     * @param mesageCallback 消息回显
     * @param <T>            任务类型
     */
    protected <T extends Task<?>> void startAsyncTask(@Nonnull final T asyncTask, @Nullable final StringProperty mesageCallback) {
        if (Objects.nonNull(mesageCallback)) {
            mesageCallback.bind(asyncTask.messageProperty());
        }
        this.startAsyncRun(asyncTask);
    }

    /**
     * 主线程执行处理
     *
     * @param handler 执行处理器
     */
    protected final void mainRun(@Nonnull final Runnable handler) {
        JfxUtils.mainRun(handler);
    }
}
