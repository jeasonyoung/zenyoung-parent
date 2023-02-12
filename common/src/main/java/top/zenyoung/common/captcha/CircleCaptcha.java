package top.zenyoung.common.captcha;

import top.zenyoung.common.util.GraphicsUtils;
import top.zenyoung.common.util.ImageUtils;
import top.zenyoung.common.util.RandomUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 圆圈干扰验证码
 *
 * @author young
 */
public class CircleCaptcha extends BaseCaptcha {
    /**
     * 构造
     *
     * @param width  图片宽
     * @param height 图片高
     */
    public CircleCaptcha(final int width, final int height) {
        this(width, height, 5);
    }

    /**
     * 构造
     *
     * @param width     图片宽
     * @param height    图片高
     * @param codeCount 字符个数
     */
    public CircleCaptcha(final int width, final int height, final int codeCount) {
        this(width, height, codeCount, 15);
    }

    /**
     * 构造
     *
     * @param width          图片宽
     * @param height         图片高
     * @param codeCount      字符个数
     * @param interfereCount 验证码干扰元素个数
     */
    public CircleCaptcha(final int width, final int height, final int codeCount, final int interfereCount) {
        super(width, height, codeCount, interfereCount);
    }

    @Override
    public Image createImage(final String code) {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = ImageUtils.createGraphics(image, Objects.isNull(this.background) ? Color.WHITE : this.background);
        // 随机画干扰圈圈
        drawInterfere(g);
        // 画字符串
        drawString(g, code);
        //
        return image;
    }

    /**
     * 绘制字符串
     *
     * @param g    {@link Graphics2D}画笔
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
     * 画随机干扰
     *
     * @param g {@link Graphics2D}
     */
    private void drawInterfere(final Graphics2D g) {
        final ThreadLocalRandom random = RandomUtils.getRandom();
        for (int i = 0; i < this.interfereCount; i++) {
            g.setColor(ImageUtils.randomColor(random));
            g.drawOval(random.nextInt(width), random.nextInt(height), random.nextInt(height >> 1), random.nextInt(height >> 1));
        }
    }
}
