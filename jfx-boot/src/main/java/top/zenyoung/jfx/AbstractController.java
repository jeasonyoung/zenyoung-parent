package top.zenyoung.jfx;

import javafx.concurrent.Task;
import javafx.scene.Node;
import lombok.AccessLevel;
import lombok.Getter;
import top.zenyoung.common.util.ThreadUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;

/**
 * jfx-控制器基类
 */
public abstract class AbstractController implements Controller {
    private static final Executor POOLS = ThreadUtils.createPools();

    @Getter(AccessLevel.PROTECTED)
    private Node root;

    @Override
    public void setRoot(@Nonnull final Node root) {
        this.root = root;
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
        this.startAsyncRun(asyncTask);
    }
}
