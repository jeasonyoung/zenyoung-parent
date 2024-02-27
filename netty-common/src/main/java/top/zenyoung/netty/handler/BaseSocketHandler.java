package top.zenyoung.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.session.Session;
import top.zenyoung.netty.session.SessionFactory;
import top.zenyoung.netty.strategy.StrategyHandlerFactory;
import top.zenyoung.netty.util.NettyUtils;
import top.zenyoung.netty.util.ScopeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * Socket处理器基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseSocketHandler<M extends Message> extends ChannelInboundHandlerAdapter {
    private final AtomicLong heartbeatTotals = new AtomicLong(0L);
    private final AtomicReference<Session> refSession = new AtomicReference<>(null);

    /**
     * 获取会话对象
     *
     * @return 会话对象
     */
    @Nullable
    protected Session getSession() {
        return refSession.get();
    }

    /**
     * 获取心跳超时次数
     *
     * @return 心跳超时次数
     */
    protected abstract Integer getHeartbeatTimeoutTotal();

    /**
     * 检查是否需要支持Scope prototype
     */
    public final void ensureHasScope() {
        ScopeUtils.checkPrototype(this.getClass());
    }

    @Override
    public final void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
            final IdleState state = event.state();
            if (state == IdleState.ALL_IDLE) {
                final long total = this.heartbeatTotals.incrementAndGet();
                if (total == Long.MAX_VALUE) {
                    this.heartbeatTotals.set(0);
                    return;
                }
                final Integer max = getHeartbeatTimeoutTotal();
                if (Objects.nonNull(max) && max > 0 && total > max) {
                    //关闭通道
                    ctx.close();
                    return;
                }
                //心跳处理
                this.heartbeatIdleHandle(ctx, getSession(), state);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 心跳处理
     *
     * @param session Session
     * @param state   IdleState
     */
    protected void heartbeatIdleHandle(@Nonnull final ChannelHandlerContext ctx,
                                       @Nullable final Session session, @Nonnull final IdleState state) {
        log.debug("heartbeatIdleHandle[{}](session: {},state: {})...", NettyUtils.getChannelId(ctx), session, state);
    }


    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        if (!ctx.channel().config().isAutoRead()) {
            ctx.read();
        }
    }

    @Override
    public final void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        //解析数据
        final M data = receivedMessageConvert(ctx, msg);
        if (Objects.isNull(data)) {
            super.channelRead(ctx, msg);
            return;
        }
        final long start = System.currentTimeMillis();
        try {
            //检查心跳超时
            if (heartbeatTotals.get() > 0) {
                heartbeatTotals.set(0);
            }
            //检查是否已创建会话
            if (Objects.isNull(getSession())) {
                //设备ID转换
                final String deviceId = buildSessionBefore(data);
                //创建会话
                refSession.set(SessionFactory.of(ctx.channel(), deviceId));
                //存储会话
                this.buildSessionAfter(getSession());
                //调用业务处理
                this.messageReceived(ctx, data);
                return;
            }
            //调用业务处理
            this.messageReceived(ctx, data);
        } finally {
            final long totals = System.currentTimeMillis() - start;
            final String channelId = NettyUtils.getChannelId(ctx);
            log.info("[{}][{}]消息通道[{}]处理耗时: {}ms", data.getCommand(), data.getDeviceId(), channelId, totals);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected M receivedMessageConvert(@Nonnull final ChannelHandlerContext ctx, @Nonnull final Object msg) {
        return (M) msg;
    }

    /**
     * 构建Session之之前
     *
     * @param data 数据
     * @return 会话设备ID
     */
    protected String buildSessionBefore(@Nonnull final M data) {
        return data.getDeviceId();
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
    protected final void messageReceived(@Nonnull final ChannelHandlerContext ctx, @Nonnull final M msg) {
        //结果消息处理
        final BiConsumer<String, Message> callbackSendHandler = (prefix, callback) -> {
            if (Objects.isNull(callback)) {
                return;
            }
            //发送反馈消息
            NettyUtils.writeAndFlush(ctx, callback, f -> {
                final boolean ret = f.isSuccess();
                log.info("[{}][{}]发送消息反馈[deviceId: {}]=> {}", callback.getCommand(), prefix, callback.getDeviceId(),
                        (ret ? "成功" : "失败," + f.cause().getMessage()));
                if (ret) {
                    final Channel channel = f.channel();
                    if (!channel.config().isAutoRead()) {
                        channel.read();
                    }
                }
            });
        };
        //获取当前会话
        final Session session = getSession();
        if (Objects.isNull(session)) {
            log.error("messageReceived[{}](msg: {})-未获取到Session,无法执行策略处理器.", NettyUtils.getChannelId(ctx), msg);
            return;
        }
        //全局策略处理器
        final M callback = globalStrategyProcess(session, msg);
        if (Objects.nonNull(callback)) {
            callbackSendHandler.accept("global-strategy", callback);
            return;
        }
        //根据消息执行策略命令
        final StrategyHandlerFactory handlerFactory = getStrategyHandlerFactory();
        Assert.notNull(handlerFactory, "未注册策略处理器工厂");
        handlerFactory.process(session, msg, cb -> callbackSendHandler.accept("strategy-handler", cb));
    }

    /**
     * 获取策略处理工厂
     *
     * @return 策略处理工厂
     */
    protected abstract StrategyHandlerFactory getStrategyHandlerFactory();

    /**
     * 全局业务策略处理器
     *
     * @param session Session会话
     * @param req     请求数据
     * @return 响应数据(为空则后续业务处理)
     */
    protected M globalStrategyProcess(@Nonnull final Session session, @Nonnull final M req) {
        return null;
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        log.warn("channelInactive:通道失效: {}", ctx);
        this.close();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        log.warn("exceptionCaught-发生异常({})-exp: {}", getSession(), cause.getMessage());
        this.close();
    }

    /**
     * 关闭通道
     */
    protected void close() {
        //移除会话
        Optional.ofNullable(getSession())
                .ifPresent(this::close);
    }

    /**
     * 关闭通道
     *
     * @param session 通道会话
     */
    protected abstract void close(@Nonnull final Session session);
}
