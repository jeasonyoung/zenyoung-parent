package top.zenyoung.graphics.captcha;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import top.zenyoung.graphics.captcha.generator.CodeGenerator;
import top.zenyoung.graphics.captcha.generator.RandomGenerator;
import top.zenyoung.graphics.util.FontUtils;
import top.zenyoung.graphics.util.ImageUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 抽象验证码<br>
 * * 抽象验证码实现了验证码字符串的生成、验证，验证码图片的写出<br>
 * * 实现类通过实现{@link #createImage(String)} 方法生成图片对象
 *
 * @author young
 */
@Data
@Slf4j
public abstract class BaseCaptcha implements Captcha {
    /**
     * 图片的宽度
     */
    protected int width;
    /**
     * 图片的高度
     */
    protected int height;
    /**
     * 验证码干扰元素个数
     */
    protected int interfereCount;
    /**
     * 字体
     */
    protected Font font;
    /**
     * 验证码
     */
    protected String code;
    /**
     * 验证码图片
     */
    protected byte[] imageBytes;
    /**
     * 验证码生成器
     */
    protected CodeGenerator generator;
    /**
     * 背景色
     */
    protected Color background;
    /**
     * 文字透明度
     */
    protected AlphaComposite textAlpha;

    /**
     * 获取随机处理器
     */
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    /**
     * 构造，使用随机验证码生成器生成验证码
     *
     * @param width          图片宽
     * @param height         图片高
     * @param codeCount      字符个数
     * @param interfereCount 验证码干扰元素个数
     */
    public BaseCaptcha(final int width, final int height, final int codeCount, final int interfereCount) {
        this(width, height, new RandomGenerator(codeCount), interfereCount);
    }

    /**
     * 构造
     *
     * @param width          图片宽
     * @param height         图片高
     * @param generator      验证码生成器
     * @param interfereCount 验证码干扰元素个数
     */
    public BaseCaptcha(final int width, final int height, @Nonnull final CodeGenerator generator, final int interfereCount) {
        this.width = width;
        this.height = height;
        this.generator = generator;
        this.interfereCount = interfereCount;
        // 字体高度设为验证码高度-2，留边距
        this.font = FontUtils.createSansSerifFont((int) (this.height * 0.75));
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limit 限制随机数的范围，不包括这个数
     * @return 随机数
     * @see Random#nextInt(int)
     */
    protected final int randomInt(final int limit) {
        return random.nextInt(limit);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param min 最小数（包含）
     * @param max 最大数（不包含）
     * @return 随机数
     */
    protected final int randomInt(final int min, final int max) {
        return random.nextInt(min, max);
    }

    @Override
    public void createCode(@Nullable final Integer len) {
        generateCode(len);
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageUtils.writePng(createImage(this.code), out);
            this.imageBytes = out.toByteArray();
        } catch (IOException e) {
            log.error("createCode()-exp: {}", e.getMessage());
        }
    }

    /**
     * 生成验证码字符串
     */
    protected void generateCode(@Nullable final Integer len) {
        this.code = generator.generate(len);
    }

    /**
     * 根据生成的code创建验证码图片
     *
     * @param code 验证码
     * @return Image
     */
    protected abstract Image createImage(@Nonnull final String code);

    @Override
    public String getCode() {
        if (null == this.code) {
            createCode(null);
        }
        return this.code;
    }

    @Override
    public void write(@Nonnull final OutputStream out) throws IOException {
        IOUtils.write(getImageBytes(), out);
    }

    /**
     * 获取图形验证码图片bytes
     *
     * @return 图形验证码图片bytes
     */
    public byte[] getImageBytes() {
        if (null == this.imageBytes) {
            createCode(null);
        }
        return this.imageBytes;
    }

    /**
     * 获取验证码图
     *
     * @return 验证码图
     */
    public BufferedImage getImage() throws IOException {
        return ImageIO.read(new ByteArrayInputStream(getImageBytes()));
    }

    /**
     * 获得图片的Base64形式
     *
     * @return 图片的Base64
     */
    @Override
    public String getImageBase64() {
        return Base64.encodeBase64String(getImageBytes());
    }

    /**
     * 获取图片带文件格式的 Base64
     *
     * @return 图片带文件格式的 Base64
     */
    @Override
    public String getImageBase64Data() {
        return ImageUtils.getDataUri("image/png", null, "base64", getImageBase64());
    }

    @Override
    public boolean verify(@Nonnull final String captchaCode, @Nonnull final String inputCode) {
        return this.generator.verify(captchaCode, inputCode);
    }

    /**
     * 设置文字透明度
     *
     * @param textAlpha 文字透明度，取值0~1，1表示不透明
     */
    public void setTextAlpha(final float textAlpha) {
        this.textAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha);
    }
}
