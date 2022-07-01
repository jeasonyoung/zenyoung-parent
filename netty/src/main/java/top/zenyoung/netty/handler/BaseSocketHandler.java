package top.zenyoung.netty.handler;

import com.google.common.base.Strings;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.config.NettyProperites;
import top.zenyoung.netty.event.ChannelClosedEvent;
import top.zenyoung.netty.event.IdleStateChangeEvent;
import top.zenyoung.netty.server.StrategyFactory;
import top.zenyoung.netty.session.Session;
import top.zenyoung.netty.session.SessionFactory;
import top.zenyoung.netty.session.SessionMap;
import top.zenyoung.netty.util.BeanUtils;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Socket业务处理接口实现
 *
 * @author young
 */
@Slf4j
@Scope(SocketHandler.SCOPE_PROTOTYPE)
public abstract class BaseSocketHandler<T extends Message> extends ChannelInboundHandlerAdapter implements SocketHandler {
    private final AtomicLong heartbeatTotals = new AtomicLong(0L);

    @Autowired
    private NettyProperites properites;

    @Autowired
    private StrategyFactory strategyFactory;

    @Autowired
    private ApplicationContext context;

    private Session session;

    public BaseSocketHandler() {
        this.ensureHasScope();
    }

    protected final void ensureHasScope() {
        BeanUtils.checkScopePrototype(this.getClass());
    }

    @Override
    public final void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            final IdleState state = ((IdleStateEvent) evt).state();
            if (Objects.nonNull(state)) {
                //检查是否读写空闲
                if (state == IdleState.ALL_IDLE) {
                    final long total = this.heartbeatTotals.incrementAndGet();
                    if (total >= Long.MAX_VALUE) {
                        this.heartbeatTotals.set(0);
                        return;
                    }
                    final Integer max = this.properites.getHeartbeatTimeoutTotal();
                    if (Objects.nonNull(max) && total > max) {
                        //检查Session
                        if (Objects.nonNull(this.session)) {
                            //移除会话
                            this.close();
                        } else {
                            //关闭通道
                            ctx.close();
                        }
                        return;
                    }
                }
                //心跳处理
                this.heartbeatIdleHandle(ctx, state);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    private void close() {
        if (Objects.nonNull(this.session)) {
            final String deviceId = this.session.getDeviceId(), clientIp = this.session.getClientIp();
            //移除会话
            SessionMap.remove(this.session);
            this.session = null;
            //发送设备通道关闭消息
            context.publishEvent(ChannelClosedEvent.of(deviceId, clientIp));
        }
    }

    /**
     * 心跳处理
     *
     * @param ctx   ChannelHandlerContext
     * @param state IdleState
     */
    protected final void heartbeatIdleHandle(@Nonnull final ChannelHandlerContext ctx, @Nonnull final IdleState state) {
        log.debug("heartbeatIdleHandle(ctx: {},state: {})...", ctx, state);
        this.context.publishEvent(IdleStateChangeEvent.of(this.session, state));
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public final void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final long start = System.currentTimeMillis();
        this.heartbeatTotals.set(0L);
        final T data = (T) msg;
        if (Objects.nonNull(data)) {
            final String deviceId;
            if (Objects.isNull(this.session) && !Strings.isNullOrEmpty(deviceId = data.getDeviceId())) {
                //创建会话
                this.session = SessionFactory.create(ctx.channel(), deviceId);
                //存储会话
                SessionMap.put(this.session);
            }
            try {
                //调用业务处理
                this.messageReceived(ctx, data);
            } finally {
                log.info("业务[session: {},ctx: {}]执行耗时: {}ms", this.session, ctx, (System.currentTimeMillis() - start));
            }
        }
    }

    /**
     * 消息接收处理
     *
     * @param ctx 上下文
     * @param msg 消息数据
     */
    protected final void messageReceived(@Nonnull final ChannelHandlerContext ctx, @Nonnull final T msg) {
        //根据消息执行策略命令
        if (Objects.nonNull(this.strategyFactory)) {
            //策略处理器处理
            final T res = this.strategyFactory.process(session, msg);
            if (Objects.nonNull(res)) {
                ctx.writeAndFlush(res);
            }
        }
    }

    @Override
    public final void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        log.warn("channelInactive:通道失效: {}", ctx);
        this.close();
    }

    @Override
    public final void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        log.warn("exceptionCaught-发生异常({})-exp: {}", this.session, cause.getMessage());
        this.close();
    }
}
