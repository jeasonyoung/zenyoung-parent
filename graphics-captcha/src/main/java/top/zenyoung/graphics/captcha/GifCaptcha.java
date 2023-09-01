package top.zenyoung.graphics.captcha;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.graphics.image.gif.AnimatedGifEncoder;
import top.zenyoung.graphics.util.ImageUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * Gif验证码
 *
 * @author young
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class GifCaptcha extends BaseCaptcha {
    /**
     * 量化器取样间隔 - 默认是10ms
     */
    private int quality = 10;
    /**
     * 帧循环次数
     */
    private int repeat = 0;
    /**
     * 设置随机颜色时,最小的取色范围
     */
    private int minColor = 0;
    /**
     * 设置随机颜色时，最大的取色范围
     */
    private int maxColor = 255;

    /**
     * 可以设置验证码宽度，高度的构造函数
     *
     * @param width  验证码宽度
     * @param height 验证码高度
     */
    public GifCaptcha(final int width, final int height) {
        this(width, height, 5);
    }

    /**
     * @param width     验证码宽度
     * @param height    验证码高度
     * @param codeCount 验证码个数
     */
    public GifCaptcha(final int width, final int height, final int codeCount) {
        super(width, height, codeCount, 10);
    }

    /**
     * 设置图像的颜色量化(转换质量 由GIF规范允许的最大256种颜色)。
     * 低的值(最小值= 1)产生更好的颜色,但处理显著缓慢。
     * 10是默认,并产生良好的颜色而且有以合理的速度。
     * 值更大(大于20)不产生显著的改善速度
     *
     * @param quality 大于1
     * @return this
     */
    public GifCaptcha setQuality(final int quality) {
        this.quality = Math.max(quality, 1);
        return this;
    }

    /**
     * 设置GIF帧应该播放的次数。
     * 默认是 0; 0意味着无限循环。
     * 必须在添加的第一个图像之前被调用。
     *
     * @param repeat 必须大于等于0
     * @return this
     */
    public GifCaptcha setRepeat(final int repeat) {
        if (repeat >= 0) {
            this.repeat = repeat;
        }
        return this;
    }

    /**
     * 设置验证码字符颜色
     *
     * @param maxColor 颜色
     * @return this
     */
    public GifCaptcha setMaxColor(final int maxColor) {
        this.maxColor = maxColor;
        return this;
    }

    /**
     * 设置验证码字符颜色
     *
     * @param minColor 颜色
     * @return this
     */
    public GifCaptcha setMinColor(final int minColor) {
        this.minColor = minColor;
        return this;
    }

    @Override
    public void createCode(@Nullable final Integer len) {
        generateCode(len);
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // gif编码类
            final AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
            //生成字符
            gifEncoder.start(out);
            //设置量化器取样间隔
            gifEncoder.setQuality(quality);
            // 帧延迟 (默认100)
            int delay = 100;
            //设置帧延迟
            gifEncoder.setDelay(delay);
            //帧循环次数
            gifEncoder.setRepeat(repeat);
            BufferedImage frame;
            final char[] chars = code.toCharArray();
            final Color[] fontColor = new Color[chars.length];
            for (int i = 0; i < chars.length; i++) {
                fontColor[i] = getRandomColor(minColor, maxColor);
                frame = graphicsImage(chars, fontColor, chars, i);
                gifEncoder.addFrame(frame);
                frame.flush();
            }
            gifEncoder.finish();
            this.imageBytes = out.toByteArray();
        } catch (IOException e) {
            log.error("createCode()-exp: {}", e.getMessage());
        }
    }

    @Override
    protected Image createImage(@Nonnull final String code) {
        return null;
    }

    /**
     * 画随机码图
     *
     * @param fontColor 随机字体颜色
     * @param words     字符数组
     * @param flag      透明度使用
     * @return BufferedImage
     */
    private BufferedImage graphicsImage(@Nonnull final char[] chars, @Nonnull final Color[] fontColor, @Nonnull final char[] words, final int flag) {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //或得图形上下文
        final Graphics2D g2d = image.createGraphics();
        //利用指定颜色填充背景
        g2d.setColor(Objects.isNull(this.background) ? Color.WHITE : this.background);
        g2d.fillRect(0, 0, width, height);
        // 字符的y坐标
        final float y = ((height >> 1) + (font.getSize() >> 1));
        final float m = 1.0f * (width - (chars.length * font.getSize())) / chars.length;
        //字符的x坐标
        final float x = Math.max(m / 2.0f, 2);
        g2d.setFont(font);
        // 指定透明度
        if (null != this.textAlpha) {
            g2d.setComposite(this.textAlpha);
        }
        AlphaComposite ac;
        for (int i = 0; i < chars.length; i++) {
            ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getAlpha(chars.length, flag, i));
            g2d.setComposite(ac);
            g2d.setColor(fontColor[i]);
            //绘制椭圆边框
            g2d.drawOval(randomInt(width), randomInt(height), randomInt(5, 30), 5 + randomInt(5, 30));
            g2d.drawString(words[i] + "", x + (font.getSize() + m) * i, y);
        }
        g2d.dispose();
        return image;
    }

    /**
     * 获取透明度,从0到1,自动计算步长
     *
     * @return float 透明度
     */
    private float getAlpha(final int v, final int i, final int j) {
        final int num = i + j;
        final float r = (float) 1 / v;
        final float s = (v + 1) * r;
        return num > v ? (num * r - s) : num * r;
    }

    /**
     * 通过给定范围获得随机的颜色
     *
     * @return Color 获得随机的颜色
     */
    private Color getRandomColor(int min, int max) {
        if (min > 255) {
            min = 255;
        }
        if (max > 255) {
            max = 255;
        }
        if (min < 0) {
            min = 0;
        }
        if (max < 0) {
            max = 0;
        }
        if (min > max) {
            min = 0;
            max = 255;
        }
        return new Color(randomInt(min, max), randomInt(min, max), randomInt(min, max));
    }

    @Override
    public String getImageBase64Data() {
        return ImageUtils.getDataUri("image/gif", null, "base64", getImageBase64());
    }
}
