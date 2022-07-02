package top.zenyoung.netty.server.handler;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import top.zenyoung.netty.server.config.NettyProperites;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * IP地址过滤器
 *
 * @author young
 */
@Slf4j
public class IpAddrFilter extends ChannelInboundHandlerAdapter {
    private final List<IpFilterRule> backRules;
    private final List<IpFilterRule> whiteRules;

    public IpAddrFilter(@Nonnull final NettyProperites properites) {
        this.backRules = splitToRules(properites.getIpAddrBlackList(), IpFilterRuleType.REJECT);
        this.whiteRules = splitToRules(properites.getIpAddrWhiteList(), IpFilterRuleType.ACCEPT);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        //黑,白名单都不存在则全部放行
        if (CollectionUtils.isEmpty(this.backRules) && CollectionUtils.isEmpty(this.whiteRules)) {
            super.channelActive(ctx);
            return;
        }
        final InetSocketAddress clientAddr = (InetSocketAddress) ctx.channel().remoteAddress();
        if (Objects.isNull(clientAddr)) {
            throw new IllegalStateException("客户端IP地址不存在! " + ctx.channel());
        }
        //检查黑名单
        if (!CollectionUtils.isEmpty(this.backRules)) {
            for (IpFilterRule rule : this.backRules) {
                if (rule.matches(clientAddr)) {
                    //关闭通道
                    ctx.close();
                    throw new IllegalStateException("IP[" + clientAddr.getHostName() + "]属于黑名单禁止访问!");
                }
            }
        }
        //检查白名单
        if (CollectionUtils.isEmpty(this.whiteRules)) {
            //白名单为空,全部放行
            super.channelActive(ctx);
            return;
        }
        //白名单处理
        for (IpFilterRule rule : this.whiteRules) {
            if (rule.matches(clientAddr)) {
                //在白名单中,放行
                super.channelActive(ctx);
                break;
            }
        }
        //不在白名单内的,关闭通道
        ctx.close();
        throw new IllegalStateException("IP[" + clientAddr.getHostName() + "]不在白名单内,禁止访问!");
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        if (Objects.nonNull(cause)) {
            log.warn(cause.getMessage());
        }
        super.exceptionCaught(ctx, cause);
    }

    private List<IpFilterRule> splitToRules(@Nonnull final List<String> ipAddrs, @Nonnull final IpFilterRuleType ruleType) {
        if (CollectionUtils.isEmpty(ipAddrs)) {
            return null;
        }
        final String sep = ",";
        return ipAddrs.stream()
                .map(ip -> {
                    if (!Strings.isNullOrEmpty(ip)) {
                        if (ip.contains(sep)) {
                            return Splitter.on(sep).omitEmptyStrings().trimResults().splitToList(ip);
                        }
                        return Lists.newArrayList(ip);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(ip -> !Strings.isNullOrEmpty(ip))
                .distinct()
                .map(ip -> new IpSubnetFilterRule(ip, 0, ruleType))
                .collect(Collectors.toList());
    }
}