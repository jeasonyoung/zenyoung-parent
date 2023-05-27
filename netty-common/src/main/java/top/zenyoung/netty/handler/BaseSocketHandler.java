package top.zenyoung.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.session.Session;
import top.zenyoung.netty.session.SessionFactory;
import top.zenyoung.netty.util.NettyUtils;
import top.zenyoung.netty.util.ScopeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 * Socket处理器基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseSocketHandler<T extends Message> extends ChannelInboundHandlerAdapter {
    private final AtomicLong heartbeatTotals = new AtomicLong(0L);
    private Session session;

    /**
     * 获取会话对象
     *
     * @return 会话对象
     */
    @Nullable
    protected Session getSession() {
        return session;
    }

    /**
     * 获取心跳超时次数
     *
     * @return 心跳超时次数
     */
    protected abstract Integer getHeartbeatTimeoutTotal();

    /**
     * 获取策略工厂
     *
     * @return 策略工厂
     */
    @Nonnull
    protected abstract StrategyFactory getStrategyFactory();

    /**
     * 检查是否需要支持Scope prototype
     */
    public final void ensureHasScope() {
        ScopeUtils.checkPrototype(this.getClass());
    }

    @Override
    public final void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            Optional.ofNullable(((IdleStateEvent) evt).state())
                    .ifPresent(state -> {
                        //检查是否读写空闲
                        if (state == IdleState.ALL_IDLE) {
                            final long total = this.heartbeatTotals.incrementAndGet();
                            if (total == Long.MAX_VALUE) {
                                this.heartbeatTotals.set(0);
                                return;
                            }
                            final boolean ret = Optional.ofNullable(getHeartbeatTimeoutTotal())
                                    .filter(max -> total > max)
                                    .map(max -> {
                                        //检查Session
                                        if (Objects.nonNull(this.session)) {
                                            //移除会话
                                            this.close();
                                        } else {
                                            //关闭通道
                                            ctx.close();
                                        }
                                        return true;
                                    })
                                    .orElse(false);
                            if (ret) {
                                return;
                            }
                        }
                        //心跳处理
                        this.heartbeatIdleHandle(ctx, session, state);
                    });
        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 心跳处理
     *
     * @param session Session
     * @param state   IdleState
     */
    protected void heartbeatIdleHandle(@Nonnull final ChannelHandlerContext ctx, @Nonnull final Session session, @Nonnull final IdleState state) {
        log.debug("heartbeatIdleHandle(session: {},state: {})...", session, state);
    }

    /**
     * 构建Session之之前
     *
     * @param rawDeviceId 原始设备ID
     * @return 会话设备ID
     */
    protected String buildSessionBefore(@Nonnull final String rawDeviceId) {
        return rawDeviceId;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected T receivedMessageConvert(@Nonnull final Object msg) {
        return (T) msg;
    }

    @Override
    public final void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        final long start = System.currentTimeMillis();
        try {
            this.heartbeatTotals.set(0L);
            Optional.ofNullable(receivedMessageConvert(msg))
                    .ifPresent(data -> {
                        //设备ID转换
                        final String deviceId = buildSessionBefore(data.getDeviceId());
                        Assert.hasText(deviceId, "'deviceId'不能为空");
                        //检查是否已创建会话
                        if (Objects.isNull(session)) {
                            //创建会话
                            this.session = SessionFactory.of(ctx.channel(), deviceId);
                            //存储会话
                            this.buildSessionAfter(this.session);
                            //调用业务处理
                            this.messageReceived(ctx, data);
                            return;
                        }
                        //检查会话设备与当前请求设备ID是否一致
                        Assert.isTrue(deviceId.equalsIgnoreCase(session.getDeviceId()),
                                "当前请求数据设备ID[" + deviceId + "]与会话设备ID[" + session.getDeviceId() + "]不一致,请求非法!");
                        //调用业务处理
                        this.messageReceived(ctx, data);
                    });
        } finally {
            log.info("[消息处理耗时: {}ms]", (System.currentTimeMillis() - start));
        }
    }

    /**
     * 构建Session之后
     *
     * @param session 通道会话
     */
    protected void buildSessionAfter(@Nonnull final Session session) {
    }

    /**
     * 消息接收处理
     *
     * @param ctx 上下文
     * @param msg 消息数据
     */
    protected final void messageReceived(@Nonnull final ChannelHandlerContext ctx, @Nonnull final T msg) {
        //结果消息处理
        final BiConsumer<String, T> callbackSendHandler = (prefix, callback) -> {
            if (Objects.isNull(callback)) {
                return;
            }
            //发送反馈消息
            NettyUtils.writeAndFlush(ctx, callback, f -> {
                final boolean ret = f.isSuccess();
                log.info("[{}]发送消息反馈[cmd: {},deviceId: {}]=> {}", prefix, callback.getCommand(), callback.getDeviceId(),
                        (ret ? "成功" : "失败," + f.cause().getMessage()));
                if (ret) {
                    f.channel().read();
                }
            });
        };
        //全局策略处理器
        T callback = globalStrategyProcess(session, msg);
        if (Objects.nonNull(callback)) {
            callbackSendHandler.accept("global", callback);
            return;
        }
        //根据消息执行策略命令
        final StrategyFactory factory = getStrategyFactory();
        Assert.notNull(factory, "'strategyFactory'不能为空");
        callback = factory.process(session, msg);
        if (Objects.nonNull(callback)) {
            callbackSendHandler.accept("factory", callback);
        }
    }

    /**
     * 全局业务策略处理器
     *
     * @param session Session会话
     * @param req     请求数据
     * @return 响应数据(为空则后续业务处理)
     */
    protected T globalStrategyProcess(@Nonnull final Session session, @Nonnull final T req) {
        return null;
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        log.warn("channelInactive:通道失效: {}", ctx);
        this.close();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        log.warn("exceptionCaught-发生异常({})-exp: {}", this.session, cause.getMessage());
        this.close();
    }

    /**
     * 关闭通道
     */
    protected void close() {
        //移除会话
        Optional.ofNullable(session)
                .ifPresent(this::close);
    }

    /**
     * 关闭通道
     *
     * @param session 通道会话
     */
    protected abstract void close(@Nonnull final Session session);
}
