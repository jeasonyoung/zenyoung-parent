package top.zenyoung.netty.mbean;

import io.netty.handler.traffic.TrafficCounter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * 流量接收机
 *
 * @author young
 */
@RequiredArgsConstructor(staticName = "of")
public class TrafficAcceptor implements TrafficAcceptorMBean {
    private final TrafficCounter trafficCounter;

    @Override
    public long getWrittenBytesThroughput() {
        return Optional.ofNullable(trafficCounter)
                .map(TrafficCounter::lastWriteThroughput)
                .orElse(0L);
    }

    @Override
    public long getReadBytesThroughput() {
        return Optional.ofNullable(trafficCounter)
                .map(TrafficCounter::lastReadThroughput)
                .orElse(0L);
    }
}
