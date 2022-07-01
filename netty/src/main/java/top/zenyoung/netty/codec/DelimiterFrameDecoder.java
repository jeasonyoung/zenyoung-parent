package top.zenyoung.netty.codec;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

/**
 * 分隔符解码器
 *
 * @author young
 */
public class DelimiterFrameDecoder extends DelimiterBasedFrameDecoder {

    /**
     * 构造函数
     *
     * @param maxFrameLength 最大帧长度
     * @param delimiters     分隔符
     */
    public DelimiterFrameDecoder(final int maxFrameLength, final String... delimiters) {
        super(maxFrameLength, Stream.of(delimiters)
                .filter(d -> !Strings.isNullOrEmpty(d))
                .map(d -> Unpooled.copiedBuffer(d, StandardCharsets.UTF_8))
                .toArray(ByteBuf[]::new)
        );
    }
}
