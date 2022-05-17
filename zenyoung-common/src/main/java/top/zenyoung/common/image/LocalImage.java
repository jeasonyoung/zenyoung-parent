package top.zenyoung.common.image;

import com.google.common.base.Strings;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.ImageFilter;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

/**
 * 图像编辑器
 *
 * @author young
 */
public class LocalImage implements Serializable {
    private final BufferedImage srcImage;
    private Image targetImage;
    /**
     * 目标图片文件格式，用于写出
     */
    private String targetImageType;
    /**
     * 计算x,y坐标的时候是否从中心做为原始坐标开始计算
     */
    private boolean positionBaseCentre = true;
    /**
     * 图片输出质量，用于压缩
     */
    private float quality = -1;

    /**
     * 从Path读取图片并开始处理
     *
     * @param imagePath 图片文件路径
     * @return Img
     */
    public static LocalImage from(final Path imagePath) throws IOException {
        return from(imagePath.toFile());
    }

    /**
     * 从文件读取图片并开始处理
     *
     * @param imageFile 图片文件
     * @return Img
     */
    public static LocalImage from(final File imageFile) throws IOException {
        return new LocalImage(ImageUtils.read(imageFile));
    }

    /**
     * 从流读取图片并开始处理
     *
     * @param in 图片流
     * @return Img
     */
    public static LocalImage from(final InputStream in) throws IOException {
        return new LocalImage(ImageUtils.read(in));
    }

    /**
     * 从ImageInputStream取图片并开始处理
     *
     * @param imageStream 图片流
     * @return Img
     */
    public static LocalImage from(final ImageInputStream imageStream) throws IOException {
        return new LocalImage(ImageUtils.read(imageStream));
    }

    /**
     * 从URL取图片并开始处理
     *
     * @param imageUrl 图片URL
     * @return Img
     */
    public static LocalImage from(final URL imageUrl) throws IOException {
        return new LocalImage(ImageUtils.read(imageUrl));
    }

    /**
     * 从Image取图片并开始处理
     *
     * @param image 图片
     * @return Img
     */
    public static LocalImage from(final Image image) {
        return new LocalImage(ImageUtils.toBufferedImage(image));
    }

    /**
     * 构造，目标图片类型取决于来源图片类型
     *
     * @param srcImage 来源图片
     */
    public LocalImage(final BufferedImage srcImage) {
        this(srcImage, null);
    }

    /**
     * 构造
     *
     * @param srcImage        来源图片
     * @param targetImageType 目标图片类型，null则读取来源图片类型
     */
    public LocalImage(final BufferedImage srcImage, String targetImageType) {
        this.srcImage = srcImage;
        if (null == targetImageType) {
            if (srcImage.getType() == BufferedImage.TYPE_INT_ARGB
                    || srcImage.getType() == BufferedImage.TYPE_INT_ARGB_PRE
                    || srcImage.getType() == BufferedImage.TYPE_4BYTE_ABGR
                    || srcImage.getType() == BufferedImage.TYPE_4BYTE_ABGR_PRE
            ) {
                targetImageType = ImageUtils.IMAGE_TYPE_PNG;
            } else {
                targetImageType = ImageUtils.IMAGE_TYPE_JPG;
            }
        }
        this.targetImageType = targetImageType;
    }

    /**
     * 设置目标图片文件格式，用于写出
     *
     * @param imgType 图片格式
     * @return this
     * @see ImageUtils#IMAGE_TYPE_JPG
     * @see ImageUtils#IMAGE_TYPE_PNG
     */
    public LocalImage setTargetImageType(final String imgType) {
        this.targetImageType = imgType;
        return this;
    }

    /**
     * 计算x,y坐标的时候是否从中心做为原始坐标开始计算
     *
     * @param positionBaseCentre 是否从中心做为原始坐标开始计算
     * @return this
     */
    public LocalImage setPositionBaseCentre(final boolean positionBaseCentre) {
        this.positionBaseCentre = positionBaseCentre;
        return this;
    }

    /**
     * 设置图片输出质量，数字为0~1（不包括0和1）表示质量压缩比，除此数字外设置表示不压缩
     *
     * @param quality 质量，数字为0~1（不包括0和1）表示质量压缩比，除此数字外设置表示不压缩
     * @return this
     */
    public LocalImage setQuality(final double quality) {
        return setQuality((float) quality);
    }

    /**
     * 设置图片输出质量，数字为0~1（不包括0和1）表示质量压缩比，除此数字外设置表示不压缩
     *
     * @param quality 质量，数字为0~1（不包括0和1）表示质量压缩比，除此数字外设置表示不压缩
     * @return this
     */
    public LocalImage setQuality(final float quality) {
        if (quality > 0 && quality < 1) {
            this.quality = quality;
        } else {
            this.quality = 1;
        }
        return this;
    }

    /**
     * 缩放图像（按比例缩放）
     *
     * @param scale 缩放比例。比例大于1时为放大，小于1大于0为缩小
     * @return this
     */
    public LocalImage scale(float scale) {
        if (scale < 0) {
            // 自动修正负数
            scale = -scale;
        }
        final Image srcImg = getValidSrcImg();
        // PNG图片特殊处理
        if (ImageUtils.IMAGE_TYPE_PNG.equals(this.targetImageType)) {
            // 修正float转double导致的精度丢失
            final double scaleDouble = Double.parseDouble(((Number) scale).toString());
            this.targetImage = ImageUtils.transform(
                    AffineTransform.getScaleInstance(scaleDouble, scaleDouble),
                    ImageUtils.toBufferedImage(srcImg, this.targetImageType)
            );
        } else {
            // 缩放后的图片宽
            final int width = ImageUtils.multi(srcImg.getWidth(null), scale).intValue();
            // 缩放后的图片高
            final int height = ImageUtils.multi(srcImg.getHeight(null), scale).intValue();
            scale(width, height);
        }
        return this;
    }

    /**
     * 缩放图像（按长宽缩放）<br>
     * 注意：目标长宽与原图不成比例会变形
     *
     * @param width  目标宽度
     * @param height 目标高度
     * @return this
     */
    public LocalImage scale(final int width, final int height) {
        return scale(width, height, Image.SCALE_SMOOTH);
    }

    /**
     * 缩放图像（按长宽缩放）<br>
     * 注意：目标长宽与原图不成比例会变形
     *
     * @param width     目标宽度
     * @param height    目标高度
     * @param scaleType 缩放类型，可选{@link Image#SCALE_SMOOTH}平滑模式或{@link Image#SCALE_DEFAULT}默认模式
     * @return this
     */
    public LocalImage scale(final int width, final int height, final int scaleType) {
        final Image srcImg = getValidSrcImg();
        final int srcHeight = srcImg.getHeight(null);
        final int srcWidth = srcImg.getWidth(null);
        if (srcHeight == height && srcWidth == width) {
            // 源与目标长宽一致返回原图
            this.targetImage = srcImg;
            return this;
        }
        if (ImageUtils.IMAGE_TYPE_PNG.equals(this.targetImageType)) {
            // png特殊处理，借助AffineTransform可以实现透明度保留
            // 宽度缩放比
            final double sx = width / (double) srcWidth;
            // 高度缩放比
            final double sy = height / (double) srcHeight;
            this.targetImage = ImageUtils.transform(AffineTransform.getScaleInstance(sx, sy), ImageUtils.toBufferedImage(srcImg, this.targetImageType));
        } else {
            this.targetImage = srcImg.getScaledInstance(width, height, scaleType);
        }
        return this;
    }

    /**
     * 等比缩放图像，此方法按照按照给定的长宽等比缩放图片，按照长宽缩放比最多的一边等比缩放，空白部分填充背景色<br>
     * 缩放后默认为jpeg格式
     *
     * @param width      缩放后的宽度
     * @param height     缩放后的高度
     * @param fixedColor 比例不对时补充的颜色，不补充为{@code null}
     * @return this
     */
    public LocalImage scale(final int width, final int height, final Color fixedColor) {
        Image srcImage = getValidSrcImg();
        int srcHeight = srcImage.getHeight(null), srcWidth = srcImage.getWidth(null);
        final double heightRatio = height / (double) srcHeight, widthRatio = width / (double) srcWidth;
        // 浮点数之间的等值判断,基本数据类型不能用==比较,包装数据类型不能用equals来判断。
        if (Double.doubleToLongBits(heightRatio) == Double.doubleToLongBits(widthRatio)) {
            // 长宽都按照相同比例缩放时，返回缩放后的图片
            scale(width, height);
        } else if (widthRatio < heightRatio) {
            // 宽缩放比例多就按照宽缩放
            scale(width, (int) (srcHeight * widthRatio));
        } else {
            // 否则按照高缩放
            scale((int) (srcWidth * heightRatio), height);
        }
        // 获取缩放后的新的宽和高
        srcImage = getValidSrcImg();
        srcHeight = srcImage.getHeight(null);
        srcWidth = srcImage.getWidth(null);
        //
        final BufferedImage image = new BufferedImage(width, height, getTypeInt());
        final Graphics2D g = image.createGraphics();
        // 设置背景
        if (null != fixedColor) {
            g.setBackground(fixedColor);
            g.clearRect(0, 0, width, height);
        }
        // 在中间贴图
        g.drawImage(srcImage, (width - srcWidth) / 2, (height - srcHeight) / 2, srcWidth, srcHeight, fixedColor, null);
        g.dispose();
        this.targetImage = image;
        return this;
    }

    /**
     * 图像切割(按指定起点坐标和宽高切割)
     *
     * @param rectangle 矩形对象，表示矩形区域的x，y，width，height
     * @return this
     */
    public LocalImage cut(final Rectangle rectangle) {
        final Image srcImage = getValidSrcImg();
        fixRectangle(rectangle, srcImage.getWidth(null), srcImage.getHeight(null));
        final ImageFilter cropFilter = new CropImageFilter(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        this.targetImage = ImageUtils.filter(cropFilter, srcImage);
        return this;
    }

    /**
     * 图像切割为圆形(按指定起点坐标和半径切割)，填充满整个图片（直径取长宽最小值）
     *
     * @param x 原图的x坐标起始位置
     * @param y 原图的y坐标起始位置
     * @return this
     */
    public LocalImage cut(final int x, final int y) {
        return cut(x, y, -1);
    }

    /**
     * 图像切割为圆形(按指定起点坐标和半径切割)
     *
     * @param x      原图的x坐标起始位置
     * @param y      原图的y坐标起始位置
     * @param radius 半径，小于0表示填充满整个图片（直径取长宽最小值）
     * @return this
     */
    public LocalImage cut(int x, int y, final int radius) {
        final Image srcImage = getValidSrcImg();
        final int width = srcImage.getWidth(null);
        final int height = srcImage.getHeight(null);
        // 计算直径
        final int diameter = radius > 0 ? radius * 2 : Math.min(width, height);
        final BufferedImage targetImage = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = targetImage.createGraphics();
        g.setClip(new Ellipse2D.Double(0, 0, diameter, diameter));
        if (this.positionBaseCentre) {
            x = x - width / 2 + diameter / 2;
            y = y - height / 2 + diameter / 2;
        }
        g.drawImage(srcImage, x, y, null);
        g.dispose();
        this.targetImage = targetImage;
        return this;
    }

    /**
     * 图片圆角处理
     *
     * @param arc 圆角弧度，0~1，为长宽占比
     * @return this
     */
    public LocalImage round(double arc) {
        final Image srcImage = getValidSrcImg();
        final int width = srcImage.getWidth(null);
        final int height = srcImage.getHeight(null);

        // 通过弧度占比计算弧度
        arc = ImageUtils.multi(arc, Math.min(width, height)).doubleValue();

        final BufferedImage targetImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = targetImage.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        // 抗锯齿
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, arc, arc));
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(srcImage, 0, 0, null);
        g2.dispose();
        this.targetImage = targetImage;
        return this;
    }

    /**
     * 彩色转为灰度
     *
     * @return this
     */
    public LocalImage gray() {
        this.targetImage = ImageUtils.colorConvert(ColorSpace.getInstance(ColorSpace.CS_GRAY), getValidSrcBufferedImg());
        return this;
    }

    /**
     * 彩色转为黑白二值化图片
     *
     * @return this
     */
    public LocalImage binary() {
        this.targetImage = ImageUtils.copyImage(getValidSrcImg(), BufferedImage.TYPE_BYTE_BINARY);
        return this;
    }

    /**
     * 给图片添加文字水印<br>
     * 此方法只在给定位置写出一个水印字符串
     *
     * @param pressText 水印文字
     * @param color     水印的字体颜色
     * @param font      {@link Font} 字体相关信息
     * @param x         修正值。 默认在中间，偏移量相对于中间偏移
     * @param y         修正值。 默认在中间，偏移量相对于中间偏移
     * @param alpha     透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 处理后的图像
     */
    public LocalImage pressText(final String pressText, final Color color, final Font font, final int x, final int y, final float alpha) {
        return pressText(pressText, color, font, new Point(x, y), alpha);
    }

    /**
     * 给图片添加文字水印<br>
     * 此方法只在给定位置写出一个水印字符串
     *
     * @param pressText 水印文字
     * @param color     水印的字体颜色
     * @param font      {@link Font} 字体相关信息
     * @param point     绘制字符串的位置坐标
     * @param alpha     透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 处理后的图像
     */
    public LocalImage pressText(final String pressText, final Color color, Font font, final Point point, final float alpha) {
        final BufferedImage targetImage = ImageUtils.toBufferedImage(getValidSrcImg(), this.targetImageType);
        if (null == font) {
            // 默认字体
            font = FontUtils.createSansSerifFont((int) (targetImage.getHeight() * 0.75));
        }
        final Graphics2D g = targetImage.createGraphics();
        // 透明度
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        // 绘制
        if (positionBaseCentre) {
            // 基于中心绘制
            GraphicsUtils.drawString(g, pressText, font, color, new Rectangle(point.x, point.y, targetImage.getWidth(), targetImage.getHeight()));
        } else {
            // 基于左上角绘制
            GraphicsUtils.drawString(g, pressText, font, color, point);
        }
        // 收笔
        g.dispose();
        this.targetImage = targetImage;
        return this;
    }

    /**
     * 获取文字居中高度的Y坐标（距离上边距距离）<br>
     * 此方法依赖FontMetrics，如果获取失败，默认为背景高度的1/3
     *
     * @param g                {@link Graphics2D}画笔
     * @param backgroundHeight 背景高度
     * @return 最小高度，-1表示无法获取
     */
    public static int getCenterY(final Graphics g, final int backgroundHeight) {
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
     * 绘制字符串，默认抗锯齿
     *
     * @param g      {@link Graphics}画笔
     * @param str    字符串
     * @param font   字体
     * @param color  字体颜色，{@code null} 表示使用随机颜色（每个字符单独随机）
     * @param width  字符串背景的宽度
     * @param height 字符串背景的高度
     * @return 画笔对象
     */
    public static Graphics drawString(final Graphics g, final String str, final Font font, final Color color, final int width, final int height) {
        // 抗锯齿
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        // 创建字体
        g.setFont(font);
        // 文字高度（必须在设置字体后调用）
        final int midY = getCenterY(g, height);
        if (null != color) {
            g.setColor(color);
        }
        final int len = str.length();
        final int charWidth = width / len;
        for (int i = 0; i < len; i++) {
            if (null == color) {
                // 产生随机的颜色值，让输出的每个字符的颜色值都将不同。
                g.setColor(ImageUtils.randomColor());
            }
            g.drawString(String.valueOf(str.charAt(i)), i * charWidth, midY);
        }
        return g;
    }

    /**
     * 获得字体对应字符串的长宽信息
     *
     * @param metrics {@link FontMetrics}
     * @param str     字符串
     * @return 长宽信息
     */
    public static Dimension getDimension(final FontMetrics metrics, final String str) {
        final int width = metrics.stringWidth(str);
        final int height = metrics.getAscent() - metrics.getLeading() - metrics.getDescent();
        return new Dimension(width, height);
    }

    /**
     * 给图片添加全屏文字水印
     *
     * @param pressText  水印文字，文件间的间隔使用尾部添加空格方式实现
     * @param color      水印的字体颜色
     * @param font       {@link Font} 字体相关信息
     * @param lineHeight 行高
     * @param degree     旋转角度，（单位：弧度），以圆点（0,0）为圆心，正代表顺时针，负代表逆时针
     * @param alpha      透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 处理后的图像
     */
    public LocalImage pressTextFull(final String pressText, final Color color, Font font, final int lineHeight, final int degree, final float alpha) {
        final BufferedImage targetImage = ImageUtils.toBufferedImage(getValidSrcImg(), this.targetImageType);
        if (null == font) {
            // 默认字体
            font = FontUtils.createSansSerifFont((int) (targetImage.getHeight() * 0.75));
        }
        final int targetHeight = targetImage.getHeight();
        final int targetWidth = targetImage.getWidth();

        // 创建画笔，并设置透明度和角度
        final Graphics2D g = targetImage.createGraphics();
        g.setColor(color);
        // 基于图片中心旋转
        g.rotate(Math.toRadians(degree), targetWidth >> 1, targetHeight >> 1);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));

        //获取字符串本身的长宽
        Dimension dimension;
        try {
            dimension = getDimension(g.getFontMetrics(font), pressText);
        } catch (Exception e) {
            // 此处报告bug某些情况下会抛出IndexOutOfBoundsException，在此做容错处理
            dimension = new Dimension(targetWidth / 3, targetHeight / 3);
        }
        final int intervalHeight = dimension.height * lineHeight;
        // 在画笔按照画布中心旋转后，达到45度时，上下左右会出现空白区，此处各延申长款的1.5倍实现全覆盖
        int y = -targetHeight >> 1;
        while (y < targetHeight * 1.5) {
            int x = -targetWidth >> 1;
            while (x < targetWidth * 1.5) {
                GraphicsUtils.drawString(g, pressText, font, color, new Point(x, y));
                x += dimension.width;
            }
            y += intervalHeight;
        }
        g.dispose();
        this.targetImage = targetImage;
        return this;
    }

    /**
     * 给图片添加图片水印
     *
     * @param pressImg 水印图片
     * @param x        修正值。 默认在中间，偏移量相对于中间偏移
     * @param y        修正值。 默认在中间，偏移量相对于中间偏移
     * @param alpha    透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return this
     */
    public LocalImage pressImage(final Image pressImg, final int x, final int y, final float alpha) {
        final int pressImgWidth = pressImg.getWidth(null);
        final int pressImgHeight = pressImg.getHeight(null);
        return pressImage(pressImg, new Rectangle(x, y, pressImgWidth, pressImgHeight), alpha);
    }

    /**
     * 给图片添加图片水印
     *
     * @param pressImg  水印图片
     * @param rectangle 矩形对象，表示矩形区域的x，y，width，height，x,y从背景图片中心计算
     * @param alpha     透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return this
     */
    public LocalImage pressImage(final Image pressImg, final Rectangle rectangle, final float alpha) {
        final Image targetImg = getValidSrcImg();
        this.targetImage = draw(ImageUtils.toBufferedImage(targetImg, this.targetImageType), pressImg, rectangle, alpha);
        return this;
    }

    /**
     * 旋转图片为指定角度<br>
     * 来自：http://blog.51cto.com/cping1982/130066
     *
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public LocalImage rotate(final int degree) {
        final Image image = getValidSrcImg();
        final int width = image.getWidth(null);
        final int height = image.getHeight(null);
        final Rectangle rectangle = calcRotatedSize(width, height, degree);
        final BufferedImage targetImg = new BufferedImage(rectangle.width, rectangle.height, getTypeInt());
        Graphics2D graphics2d = targetImg.createGraphics();
        // 抗锯齿
        graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        // 从中心旋转
        graphics2d.translate((rectangle.width - width) / 2D, (rectangle.height - height) / 2D);
        graphics2d.rotate(Math.toRadians(degree), width / 2D, height / 2D);
        graphics2d.drawImage(image, 0, 0, null);
        graphics2d.dispose();
        this.targetImage = targetImg;
        return this;
    }

    /**
     * 水平翻转图像
     *
     * @return this
     */
    public LocalImage flip() {
        final Image image = getValidSrcImg();
        final int width = image.getWidth(null);
        final int height = image.getHeight(null);
        final BufferedImage targetImg = new BufferedImage(width, height, getTypeInt());
        Graphics2D graphics2d = targetImg.createGraphics();
        graphics2d.drawImage(image, 0, 0, width, height, width, 0, 0, height, null);
        graphics2d.dispose();
        this.targetImage = targetImg;
        return this;
    }

    /**
     * 描边，此方法为向内描边，会覆盖图片相应的位置
     *
     * @param color 描边颜色，默认黑色
     * @param width 边框粗细
     * @return this
     */
    public LocalImage stroke(final Color color, final float width) {
        return stroke(color, new BasicStroke(width));
    }

    /**
     * 描边，此方法为向内描边，会覆盖图片相应的位置
     *
     * @param color  描边颜色，默认黑色
     * @param stroke 描边属性，包括粗细、线条类型等，见{@link BasicStroke}
     * @return this
     */
    public LocalImage stroke(final Color color, final Stroke stroke) {
        final BufferedImage image = ImageUtils.toBufferedImage(getValidSrcImg(), this.targetImageType);
        final int width = image.getWidth(null), height = image.getHeight(null);
        final Graphics2D g = image.createGraphics();
        g.setColor(Objects.isNull(color) ? Color.BLACK : color);
        if (null != stroke) {
            g.setStroke(stroke);
        }
        g.drawRect(0, 0, width - 1, height - 1);
        g.dispose();
        this.targetImage = image;
        return this;
    }

    /**
     * 获取处理过的图片
     *
     * @return 处理过的图片
     */
    public Image getImg() {
        return getValidSrcImg();
    }

    /**
     * 写出图像为结果设置格式<br>
     * 结果类型设定见{@link #setTargetImageType(String)}
     *
     * @param out 写出到的目标流
     * @return 是否成功写出，如果返回false表示未找到合适的Writer
     * @throws IOException IO异常
     */
    public boolean write(final OutputStream out) throws IOException {
        return write(ImageUtils.getImageOutputStream(out));
    }

    /**
     * 写出图像为结果设置格式<br>
     * 结果类型设定见{@link #setTargetImageType(String)}
     *
     * @param targetImageStream 写出到的目标流
     * @return 是否成功写出，如果返回false表示未找到合适的Writer
     * @throws IOException IO异常
     */
    public boolean write(final ImageOutputStream targetImageStream) throws IOException {
        if (Strings.isNullOrEmpty(this.targetImageType)) {
            throw new IllegalArgumentException("Target image type is blank !");
        }
        if (Objects.isNull(targetImageStream)) {
            throw new IllegalArgumentException("Target output stream is null");
        }
        final Image targetImage = (null == this.targetImage) ? this.srcImage : this.targetImage;
        if (Objects.isNull(targetImage)) {
            throw new IllegalArgumentException("Target image is null");
        }
        return ImageUtils.write(targetImage, this.targetImageType, targetImageStream, this.quality);
    }

    /**
     * 写出图像为目标文件扩展名对应的格式
     *
     * @param targetFile 目标文件
     * @return 是否成功写出，如果返回false表示未找到合适的Writer
     * @throws IOException IO异常
     */
    public boolean write(@Nonnull final File targetFile) throws IOException {
        final String formatName = FilenameUtils.getExtension(targetFile.getName());
        if (!Strings.isNullOrEmpty(formatName)) {
            this.targetImageType = formatName;
        }
        if (targetFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            targetFile.delete();
        }
        try (final ImageOutputStream out = ImageUtils.getImageOutputStream(targetFile)) {
            return write(out);
        }
    }


    /**
     * 将图片绘制在背景上
     *
     * @param backgroundImg 背景图片
     * @param img           要绘制的图片
     * @param rectangle     矩形对象，表示矩形区域的x，y，width，height，x,y从背景图片中心计算（如果positionBaseCentre为true）
     * @param alpha         透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     * @return 绘制后的背景
     */
    private BufferedImage draw(final BufferedImage backgroundImg, final Image img, final Rectangle rectangle, final float alpha) {
        final Graphics2D g = backgroundImg.createGraphics();
        setAlpha(g, alpha);

        fixRectangle(rectangle, backgroundImg.getWidth(), backgroundImg.getHeight());
        GraphicsUtils.drawImg(g, img, rectangle);
        g.dispose();
        return backgroundImg;
    }

    /**
     * 设置画笔透明度
     *
     * @param g     画笔
     * @param alpha 透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     */
    private static void setAlpha(final Graphics2D g, final float alpha) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
    }

    /**
     * 获取int类型的图片类型
     *
     * @return 图片类型
     * @see BufferedImage#TYPE_INT_ARGB
     * @see BufferedImage#TYPE_INT_RGB
     */
    private int getTypeInt() {
        //noinspection SwitchStatementWithTooFewBranches
        switch (this.targetImageType) {
            case ImageUtils.IMAGE_TYPE_PNG:
                return BufferedImage.TYPE_INT_ARGB;
            default:
                return BufferedImage.TYPE_INT_RGB;
        }
    }

    /**
     * 获取有效的源图片，首先检查上一次处理的结果图片，如无则使用用户传入的源图片
     *
     * @return 有效的源图片
     */
    private Image getValidSrcImg() {
        return Objects.isNull(this.targetImage) ? this.srcImage : this.targetImage;
    }

    /**
     * 获取有效的源{@link BufferedImage}图片，首先检查上一次处理的结果图片，如无则使用用户传入的源图片
     *
     * @return 有效的源图片
     */
    private BufferedImage getValidSrcBufferedImg() {
        return ImageUtils.toBufferedImage(getValidSrcImg(), this.targetImageType);
    }

    /**
     * 修正矩形框位置<br>
     * 则坐标修正为基于图形中心，否则基于左上角
     *
     * @param rectangle  矩形
     * @param baseWidth  参考宽
     * @param baseHeight 参考高
     */
    private void fixRectangle(final Rectangle rectangle, final int baseWidth, final int baseHeight) {
        if (this.positionBaseCentre) {
            final Point pointBaseCentre = ImageUtils.getPointBaseCentre(rectangle, baseWidth, baseHeight);
            // 修正图片位置从背景的中心计算
            rectangle.setLocation(pointBaseCentre.x, pointBaseCentre.y);
        }
    }

    /**
     * 计算旋转后的图片尺寸
     *
     * @param width  宽度
     * @param height 高度
     * @param degree 旋转角度
     * @return 计算后目标尺寸
     */
    private static Rectangle calcRotatedSize(int width, int height, int degree) {
        if (degree < 0) {
            // 负数角度转换为正数角度
            degree += 360;
        }
        if (degree >= 90) {
            if (degree / 90 % 2 == 1) {
                int temp = height;
                //noinspection SuspiciousNameCombination
                height = width;
                width = temp;
            }
            degree = degree % 90;
        }
        final double r = Math.sqrt(height * height + width * width) / 2;
        final double len = 2 * Math.sin(Math.toRadians(degree) / 2) * r;
        final double angel_alpha = (Math.PI - Math.toRadians(degree)) / 2;
        final double angel_dalta_width = Math.atan((double) height / width);
        final double angel_dalta_height = Math.atan((double) width / height);
        final int len_dalta_width = (int) (len * Math.cos(Math.PI - angel_alpha - angel_dalta_width));
        final int len_dalta_height = (int) (len * Math.cos(Math.PI - angel_alpha - angel_dalta_height));
        final int des_width = width + len_dalta_width * 2;
        final int des_height = height + len_dalta_height * 2;
        return new Rectangle(des_width, des_height);
    }
}
