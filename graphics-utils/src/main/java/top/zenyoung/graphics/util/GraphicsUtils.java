package top.zenyoung.graphics.util;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * 绘制相关工具类
 *
 * @author young
 */
@UtilityClass
public class GraphicsUtils {
    /**
     * 创建{@link Graphics2D}
     *
     * @param image {@link BufferedImage}
     * @param color {@link Color}背景颜色以及当前画笔颜色，{@code null}表示不设置背景色
     * @return {@link Graphics2D}
     */
    public static Graphics2D createGraphics(@Nonnull final BufferedImage image, @Nullable final Color color) {
        final Graphics2D g = image.createGraphics();
        if (null != color) {
            // 填充背景
            g.setColor(color);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
        }
        return g;
    }

    /**
     * 获取文字居中高度的Y坐标（距离上边距距离）<br>
     * 此方法依赖FontMetrics，如果获取失败，默认为背景高度的1/3
     *
     * @param g                {@link Graphics2D}画笔
     * @param backgroundHeight 背景高度
     * @return 最小高度，-1表示无法获取
     */
    public static int getCenterY(@Nonnull final Graphics g, final int backgroundHeight) {
        // 获取允许文字最小高度
        FontMetrics metrics = null;
        try {
            metrics = g.getFontMetrics();
        } catch (Exception e) {
            // 此处报告bug某些情况下会抛出IndexOutOfBoundsException，在此做容错处理
        }
        int y;
        if (null != metrics) {
            y = (backgroundHeight - metrics.getHeight()) / 2 + metrics.getAscent();
        } else {
            y = backgroundHeight / 3;
        }
        return y;
    }

    /**
     * 绘制字符串，使用随机颜色，默认抗锯齿
     *
     * @param g      {@link Graphics}画笔
     * @param text   字符串
     * @param font   字体
     * @param width  字符串总宽度
     * @param height 字符串背景高度
     * @return 画笔对象
     */
    public static Graphics drawStringColourful(@Nonnull final Graphics g, @Nonnull final String text,
                                               @Nonnull final Font font, final int width, final int height) {
        return drawString(g, text, font, null, width, height);
    }

    /**
     * 绘制字符串，默认抗锯齿
     *
     * @param g      {@link Graphics}画笔
     * @param text   字符串
     * @param font   字体
     * @param color  字体颜色，{@code null} 表示使用随机颜色（每个字符单独随机）
     * @param width  字符串背景的宽度
     * @param height 字符串背景的高度
     * @return 画笔对象
     */
    public static Graphics drawString(@Nonnull final Graphics g, @Nonnull final String text, @Nonnull final Font font,
                                      @Nullable final Color color, final int width, final int height) {
        // 抗锯齿
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        // 创建字体
        g.setFont(font);
        // 文字高度（必须在设置字体后调用）
        int midY = getCenterY(g, height);
        if (null != color) {
            g.setColor(color);
        }
        final int len = text.length();
        int charWidth = width / len;
        for (int i = 0; i < len; i++) {
            if (null == color) {
                // 产生随机的颜色值，让输出的每个字符的颜色值都将不同。
                g.setColor(ImageUtils.randomColor());
            }
            g.drawString(String.valueOf(text.charAt(i)), i * charWidth, midY);
        }
        return g;
    }

    /**
     * 绘制字符串，默认抗锯齿。<br>
     * 此方法定义一个矩形区域和坐标，文字基于这个区域中间偏移x,y绘制。
     *
     * @param g         {@link Graphics}画笔
     * @param str       字符串
     * @param font      字体，字体大小决定了在背景中绘制的大小
     * @param color     字体颜色，{@code null} 表示使用黑色
     * @param rectangle 字符串绘制坐标和大小，此对象定义了绘制字符串的区域大小和偏移位置
     * @return 画笔对象
     */
    public static Graphics drawString(@Nonnull final Graphics g, @Nonnull final String str, @Nonnull final Font font,
                                      @Nonnull final Color color, @Nonnull final Rectangle rectangle) {
        // 背景长宽
        final int backgroundWidth = rectangle.width;
        final int backgroundHeight = rectangle.height;
        //获取字符串本身的长宽
        Dimension dimension;
        try {
            dimension = FontUtils.getDimension(g.getFontMetrics(font), str);
        } catch (Exception e) {
            // 此处报告bug某些情况下会抛出IndexOutOfBoundsException，在此做容错处理
            dimension = new Dimension(backgroundWidth / 3, backgroundHeight / 3);
        }
        rectangle.setSize(dimension.width, dimension.height);
        final Point point = ImageUtils.getPointBaseCentre(rectangle, backgroundWidth, backgroundHeight);
        return drawString(g, str, font, color, point);
    }

    /**
     * 绘制字符串，默认抗锯齿
     *
     * @param g     {@link Graphics}画笔
     * @param str   字符串
     * @param font  字体，字体大小决定了在背景中绘制的大小
     * @param color 字体颜色，{@code null} 表示使用黑色
     * @param point 绘制字符串的位置坐标
     * @return 画笔对象
     */
    public static Graphics drawString(@Nonnull final Graphics g, @Nonnull final String str, @Nonnull final Font font,
                                      @Nullable final Color color, @Nonnull final Point point) {
        // 抗锯齿
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        g.setColor(Objects.isNull(color) ? Color.BLACK : color);
        g.drawString(str, point.x, point.y);
        return g;
    }

    /**
     * 绘制图片
     *
     * @param g     画笔
     * @param img   要绘制的图片
     * @param point 绘制的位置，基于左上角
     * @return 画笔对象
     */
    public static Graphics drawImg(@Nonnull final Graphics g, @Nonnull final Image img, @Nonnull final Point point) {
        return drawImg(g, img, new Rectangle(point.x, point.y, img.getWidth(null), img.getHeight(null)));
    }

    /**
     * 绘制图片
     *
     * @param g         画笔
     * @param img       要绘制的图片
     * @param rectangle 矩形对象，表示矩形区域的x，y，width，height,，基于左上角
     * @return 画笔对象
     */
    public static Graphics drawImg(@Nonnull final Graphics g, @Nonnull final Image img, @Nonnull final Rectangle rectangle) {
        // 绘制切割后的图
        g.drawImage(img, rectangle.x, rectangle.y, rectangle.width, rectangle.height, null);
        return g;
    }

    /**
     * 设置画笔透明度
     *
     * @param g     画笔
     * @param alpha 透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 画笔
     */
    public static Graphics2D setAlpha(@Nonnull final Graphics2D g, final float alpha) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        return g;
    }
}
