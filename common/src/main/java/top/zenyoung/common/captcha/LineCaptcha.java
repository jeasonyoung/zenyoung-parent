package top.zenyoung.common.captcha;

import top.zenyoung.common.image.GraphicsUtils;
import top.zenyoung.common.image.ImageUtils;
import top.zenyoung.common.util.RandomUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 使用干扰线方式生成的图形验证码
 *
 * @author young
 */
public class LineCaptcha extends BaseCaptcha {
    /**
     * 构造，默认5位验证码，150条干扰线
     *
     * @param width  图片宽
     * @param height 图片高
     */
    public LineCaptcha(final int width, final int height) {
        this(width, height, 5, 150);
    }

    /**
     * 构造
     *
     * @param width     图片宽
     * @param height    图片高
     * @param codeCount 字符个数
     * @param lineCount 干扰线条数
     */
    public LineCaptcha(final int width, final int height, final int codeCount, final int lineCount) {
        super(width, height, codeCount, lineCount);
    }

    @Override
    public Image createImage(final String code) {
        // 图像buffer
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = GraphicsUtils.createGraphics(image, Objects.isNull(this.background) ? Color.WHITE : this.background);
        // 干扰线
        drawInterfere(g);
        // 字符串
        drawString(g, code);
        //
        return image;
    }

    /**
     * 绘制字符串
     *
     * @param g    {@link Graphics}画笔
     * @param code 验证码
     */
    private void drawString(final Graphics2D g, final String code) {
        // 指定透明度
        if (null != this.textAlpha) {
            g.setComposite(this.textAlpha);
        }
        GraphicsUtils.drawStringColourful(g, code, this.font, this.width, this.height);
    }

    /**
     * 绘制干扰线
     *
     * @param g {@link Graphics2D}画笔
     */
    private void drawInterfere(final Graphics2D g) {
        final ThreadLocalRandom random = RandomUtils.getRandom();
        // 干扰线
        for (int i = 0; i < this.interfereCount; i++) {
            final int xs = random.nextInt(width);
            final int ys = random.nextInt(height);
            final int xe = xs + random.nextInt(width / 8);
            final int ye = ys + random.nextInt(height / 8);
            g.setColor(ImageUtils.randomColor(random));
            g.drawLine(xs, ys, xe, ye);
        }
    }
}
