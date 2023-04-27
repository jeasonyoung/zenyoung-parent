package top.zenyoung.netty.handler;

import com.google.common.base.Strings;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.event.ClosedEvent;
import top.zenyoung.netty.session.Session;
import top.zenyoung.netty.session.SessionFactory;
import top.zenyoung.netty.util.ScopeUtils;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Socket处理器基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseSocketHandler<T extends Message> extends ChannelInboundHandlerAdapter {
    private final AtomicLong heartbeatTotals = new AtomicLong(0L);
    private Session session;
    @Autowired
    private ApplicationContext context;

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
    protected abstract StrategyFactory getStrategyFactory();

    /**
     * 获取Bean对象
     *
     * @param cls bean类型
     * @param <R> bean对象类型
     * @return Bean对象
     */
    protected final <R> R getBean(@Nonnull final Class<R> cls) {
        return context.getBean(cls);
    }

    /**
     * 发布Spring事件
     *
     * @param event 事件数据
     * @param <E>   事件类型
     */
    protected <E> void publishContextEvent(@Nonnull final E event) {
        Optional.ofNullable(context)
                .ifPresent(ctx -> ctx.publishEvent(event));
    }

    /**
     * 检查是否需要支持Scope prototype
     */
    protected final void ensureHasScope() {
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
                        Optional.ofNullable(session)
                                .ifPresent(ses -> heartbeatIdleHandle(ctx, ses, state));
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

    @Override
    @SuppressWarnings({"unchecked"})
    public final void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        final long start = System.currentTimeMillis();
        this.heartbeatTotals.set(0L);
        Optional.ofNullable((T) msg)
                .ifPresent(data -> {
                    //检查是否已建立会话
                    final String rawDeviceId;
                    if (Objects.isNull(this.session) && !Strings.isNullOrEmpty(rawDeviceId = data.getDeviceId())) {
                        final String sessionDeviceId = buildSessionBefore(rawDeviceId);
                        if (!Strings.isNullOrEmpty(sessionDeviceId)) {
                            //创建会话
                            this.session = SessionFactory.create(ctx.channel(), sessionDeviceId, info -> {
                                //发送设备通道关闭消息
                                context.publishEvent(ClosedEvent.of(info.getDeviceId(), info.getClientIp()));
                            });
                            //存储会话
                            this.buildSessionAfter(this.session);
                        }
                    }
                    try {
                        //调用业务处理
                        this.messageReceived(ctx, data);
                    } finally {
                        log.info("业务[session: {},ctx: {}]执行耗时: {}ms", this.session, ctx, (System.currentTimeMillis() - start));
                    }
                });
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
        //全局策略处理器
        final T callback = globalStrategyProcess(session, msg);
        if (Objects.nonNull(callback)) {
            Optional.ofNullable(ctx.writeAndFlush(callback))
                    .ifPresent(future -> future.addListener(f -> log.info("消息发送=> {}", f.isSuccess())));
            return;
        }
        //根据消息执行策略命令
        Optional.ofNullable(getStrategyFactory())
                .filter(factory -> Objects.nonNull(session))
                .map(factory -> factory.process(session, msg))
                .map(ctx::writeAndFlush)
                .ifPresent(future -> future.addListener(f -> {
                    log.info("消息发送=> {}", f.isSuccess());
                    //发送消息成功后,自动读取下一消息
                    Optional.ofNullable(future.channel())
                            .ifPresent(Channel::read);
                }));
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
    public final void channelInactive(final ChannelHandlerContext ctx) {
        log.warn("channelInactive:通道失效: {}", ctx);
        this.close();
    }

    @Override
    public final void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        log.warn("exceptionCaught-发生异常({})-exp: {}", this.session, cause.getMessage());
        this.close();
    }

    /**
     * 关闭通道
     */
    protected void close() {
        Optional.ofNullable(session)
                .ifPresent(sess -> {
                    //移除会话
                    this.close(sess);
                    this.session = null;
                });
    }

    /**
     * 关闭通道
     *
     * @param session 通道会话
     */
    protected abstract void close(@Nonnull final Session session);
}
