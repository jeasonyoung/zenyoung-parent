package top.zenyoung.common.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 随机数工具类
 *
 * @author young
 */
@UtilityClass
public class RandomUtils {
    /**
     * 用于随机选的数字
     */
    public static final String BASE_NUMBER = "0123456789";
    /**
     * 用于随机选的字符
     */
    public static final String BASE_CHAR = "abcdefghijklmnopqrstuvwxyz";
    /**
     * 用于随机选的字符和数字
     */
    public static final String BASE_CHAR_NUMBER = BASE_CHAR + BASE_NUMBER;

    /**
     * 获取随机数生成器对象<br>
     * ThreadLocalRandom是JDK 7之后提供并发产生随机数，能够解决多个线程发生的竞争争夺。
     *
     * <p>
     * 注意：此方法返回的{@link ThreadLocalRandom}不可以在多线程环境下共享对象，否则有重复随机数问题。
     * 见：<a href="https://www.jianshu.com/p/89dfe990295c">...</a>
     * </p>
     *
     * @return {@link ThreadLocalRandom}
     */
    public ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * 创建{@link SecureRandom}，类提供加密的强随机数生成器 (RNG)<br>
     *
     * @param seed 自定义随机种子
     * @return {@link SecureRandom}
     */
    public SecureRandom createSecureRandom(@Nullable final byte[] seed) {
        return (null == seed) ? new SecureRandom() : new SecureRandom(seed);
    }

    /**
     * 获取SHA1PRNG的{@link SecureRandom}，类提供加密的强随机数生成器 (RNG)<br>
     * 注意：此方法获取的是伪随机序列发生器PRNG（pseudo-random number generator）
     *
     * <p>
     * 相关说明见：<a href="https://stackoverflow.com/questions/137212/how-to-solve-slow-java-securerandom">...</a>
     *
     * @return {@link SecureRandom}
     */
    public SecureRandom getSecureRandom() {
        return getSecureRandom(null);
    }

    /**
     * 获取SHA1PRNG的{@link SecureRandom}，类提供加密的强随机数生成器 (RNG)<br>
     * 注意：此方法获取的是伪随机序列发生器PRNG（pseudo-random number generator）
     *
     * <p>
     * 相关说明见：<a href="https://stackoverflow.com/questions/137212/how-to-solve-slow-java-securerandom">...</a>
     *
     * @param seed 随机数种子
     * @return {@link SecureRandom}
     * @see #createSecureRandom(byte[])
     */
    public SecureRandom getSecureRandom(@Nullable final byte[] seed) {
        return createSecureRandom(seed);
    }

    /**
     * 获取SHA1PRNG的{@link SecureRandom}，类提供加密的强随机数生成器 (RNG)<br>
     * 注意：此方法获取的是伪随机序列发生器PRNG（pseudo-random number generator）,在Linux下噪声生成时可能造成较长时间停顿。<br>
     * see: <a href="http://ifeve.com/jvm-random-and-entropy-source/">...</a>
     *
     * <p>
     * 相关说明见：<a href="https://stackoverflow.com/questions/137212/how-to-solve-slow-java-securerandom">...</a>
     *
     * @param seed 随机数种子
     * @return {@link SecureRandom}
     */
    public SecureRandom getSha1PrngRandom(@Nonnull final byte[] seed) throws NoSuchAlgorithmException {
        final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(seed);
        return random;
    }

    /**
     * 获取algorithms/providers中提供的强安全随机生成器<br>
     * 注意：此方法可能造成阻塞或性能问题
     *
     * @return {@link SecureRandom}
     */
    public SecureRandom getSecureRandomStrong() throws NoSuchAlgorithmException {
        return SecureRandom.getInstanceStrong();
    }

    /**
     * 获取随机数产生器
     *
     * @param isSecure 是否为强随机数生成器 (RNG)
     * @return {@link Random}
     * @see #getSecureRandom()
     * @see #getRandom()
     */
    public Random getRandom(final boolean isSecure) {
        return isSecure ? getSecureRandom() : getRandom();
    }

    /**
     * 获得随机Boolean值
     *
     * @return true or false
     */
    public boolean randomBoolean() {
        return 0 == randomInt(2);
    }

    /**
     * 随机汉字（'\u4E00'-'\u9FFF'）
     *
     * @return 随机的汉字字符
     */
    public char randomChinese() {
        return (char) randomInt('\u4E00', '\u9FFF');
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param min 最小数（包含）
     * @param max 最大数（不包含）
     * @return 随机数
     */
    public int randomInt(final int min, final int max) {
        return getRandom().nextInt(min, max);
    }

    /**
     * 获得随机数int值
     *
     * @return 随机数
     * @see Random#nextInt()
     */
    public int randomInt() {
        return getRandom().nextInt();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limit 限制随机数的范围，不包括这个数
     * @return 随机数
     * @see Random#nextInt(int)
     */
    public int randomInt(final int limit) {
        return getRandom().nextInt(limit);
    }

    /**
     * 获得指定范围内的随机数[min, max)
     *
     * @param min 最小数（包含）
     * @param max 最大数（不包含）
     * @return 随机数
     * @see ThreadLocalRandom#nextLong(long, long)
     */
    public long randomLong(final long min, final long max) {
        return getRandom().nextLong(min, max);
    }

    /**
     * 获得随机数
     *
     * @return 随机数
     * @see ThreadLocalRandom#nextLong()
     * @since 3.3.0
     */
    public long randomLong() {
        return getRandom().nextLong();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limit 限制随机数的范围，不包括这个数
     * @return 随机数
     * @see ThreadLocalRandom#nextLong(long)
     */
    public long randomLong(final long limit) {
        return getRandom().nextLong(limit);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param min 最小数（包含）
     * @param max 最大数（不包含）
     * @return 随机数
     * @see ThreadLocalRandom#nextDouble(double, double)
     */
    public double randomDouble(final double min, final double max) {
        return getRandom().nextDouble(min, max);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param min          最小数（包含）
     * @param max          最大数（不包含）
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 随机数
     * @since 4.0.8
     */
    public double randomDouble(final double min, final double max, final int scale,
                               @Nonnull final RoundingMode roundingMode) {
        return round(BigDecimal.valueOf(randomDouble(min, max)), scale, roundingMode).doubleValue();
    }

    /**
     * 保留固定位数小数<br>
     * 例如保留四位小数：123.456789 =》 123.4567
     *
     * @param number       数字值
     * @param scale        保留小数位数，如果传入小于0，则默认0
     * @param roundingMode 保留小数的模式 {@link RoundingMode}，如果传入null则默认四舍五入
     * @return 新值
     */
    public BigDecimal round(@Nullable final BigDecimal number, final int scale, @Nullable final RoundingMode roundingMode) {
        final BigDecimal decimal = Objects.isNull(number) ? BigDecimal.ZERO : number;
        final RoundingMode mode = Objects.isNull(roundingMode) ? RoundingMode.HALF_UP : roundingMode;
        return decimal.setScale(Math.max(scale, 0), mode);
    }

    /**
     * 获得随机数[0, 1)
     *
     * @return 随机数
     * @see ThreadLocalRandom#nextDouble()
     * @since 3.3.0
     */
    public double randomDouble() {
        return getRandom().nextDouble();
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 随机数
     * @since 4.0.8
     */
    public double randomDouble(final int scale, @Nullable final RoundingMode roundingMode) {
        return round(BigDecimal.valueOf(randomDouble()), scale, roundingMode).doubleValue();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limit 限制随机数的范围，不包括这个数
     * @return 随机数
     * @see ThreadLocalRandom#nextDouble(double)
     * @since 3.3.0
     */
    public double randomDouble(final double limit) {
        return getRandom().nextDouble(limit);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param limit        限制随机数的范围，不包括这个数
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 随机数
     * @since 4.0.8
     */
    public double randomDouble(final double limit, final int scale, @Nullable final RoundingMode roundingMode) {
        return round(BigDecimal.valueOf(randomDouble(limit)), scale, roundingMode).doubleValue();
    }

    /**
     * 获得指定范围内的随机数[0, 1)
     *
     * @return 随机数
     * @since 4.0.9
     */
    public BigDecimal randomBigDecimal() {
        return BigDecimal.valueOf(getRandom().nextDouble());
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limit 最大数（不包含）
     * @return 随机数
     * @since 4.0.9
     */
    public BigDecimal randomBigDecimal(@Nonnull final BigDecimal limit) {
        return BigDecimal.valueOf(getRandom().nextDouble(limit.doubleValue()));
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param min 最小数（包含）
     * @param max 最大数（不包含）
     * @return 随机数
     * @since 4.0.9
     */
    public BigDecimal randomBigDecimal(@Nonnull final BigDecimal min, @Nonnull final BigDecimal max) {
        return BigDecimal.valueOf(getRandom().nextDouble(min.doubleValue(), max.doubleValue()));
    }

    /**
     * 随机bytes
     *
     * @param length 长度
     * @return bytes
     */
    public byte[] randomBytes(final int length) {
        final byte[] bytes = new byte[length];
        getRandom().nextBytes(bytes);
        return bytes;
    }

    /**
     * 随机获得列表中的元素
     *
     * @param <T>  元素类型
     * @param list 列表
     * @return 随机元素
     */
    public <T> T randomEle(final Collection<T> list) {
        return randomEle(list, list.size());
    }

    /**
     * 随机获得列表中的元素
     *
     * @param <T>   元素类型
     * @param list  列表
     * @param limit 限制列表的前N项
     * @return 随机元素
     */
    public <T> T randomEle(@Nonnull final Collection<T> list, final int limit) {
        final int max = Math.min(limit, list.size());
        final int index = randomInt(max);
        return Lists.newArrayList(list).get(index);
    }

    /**
     * 随机获得数组中的元素
     *
     * @param <T>   元素类型
     * @param array 列表
     * @return 随机元素
     * @since 3.3.0
     */
    public <T> T randomEle(@Nonnull final T[] array) {
        return randomEle(array, array.length);
    }

    /**
     * 随机获得数组中的元素
     *
     * @param <T>   元素类型
     * @param array 列表
     * @param limit 限制列表的前N项
     * @return 随机元素
     * @since 3.3.0
     */
    public <T> T randomEle(@Nonnull final T[] array, final int limit) {
        final int max = Math.min(limit, array.length);
        final int index = randomInt(max);
        return array[index];
    }

    /**
     * 随机获得列表中的一定量元素
     *
     * @param <T>   元素类型
     * @param list  列表
     * @param count 随机取出的个数
     * @return 随机元素
     */
    public <T> Collection<T> randomEles(@Nonnull final Collection<T> list, final int count) {
        final List<T> result = Lists.newLinkedList();
        final int limit = list.size();
        while (result.size() < count) {
            final T item = randomEle(list, limit);
            if (!result.contains(item)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 随机获得列表中的一定量的元素，返回List<br>
     *
     * @param source 列表
     * @param count  随机取出的个数
     * @param <T>    元素类型
     * @return 随机列表
     * @since 5.2.1
     */
    public <T> Collection<T> randomEleList(@Nonnull final Collection<T> source, final int count) {
        if (count >= source.size()) {
            return source;
        }
        final List<T> items = Lists.newArrayList(source);
        final int[] randomList = sub(randomInts(source.size()), 0, count);
        final List<T> result = Lists.newLinkedList();
        for (int e : randomList) {
            result.add(items.get(e));
        }
        return result;
    }

    public int[] sub(@Nonnull final int[] array, int start, int end) {
        final int length = array.length;
        if (start < 0) {
            start += length;
        }
        if (end < 0) {
            end += length;
        }
        if (start == length) {
            return new int[0];
        }
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if (end > length) {
            if (start >= length) {
                return new int[0];
            }
            end = length;
        }
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * 新建一个空数组
     *
     * @param <T>           数组元素类型
     * @param componentType 元素类型
     * @param newSize       大小
     * @return 空数组
     */
    @SuppressWarnings("unchecked")
    public <T> T[] newArray(@Nonnull final Class<?> componentType, final int newSize) {
        return (T[]) Array.newInstance(componentType, newSize);
    }

    /**
     * 随机获得列表中的一定量的不重复元素，返回Set
     *
     * @param <T>        元素类型
     * @param collection 列表
     * @param count      随机取出的个数
     * @return 随机元素
     * @throws IllegalArgumentException 需要的长度大于给定集合非重复总数
     */
    public <T> Set<T> randomEleSet(@Nonnull final Collection<T> collection, final int count) {
        final List<T> source = collection.stream().distinct().collect(Collectors.toList());
        if (count > source.size()) {
            throw new IllegalArgumentException("Count is larger than collection distinct size !");
        }
        final Set<T> result = Sets.newLinkedHashSet();
        final int limit = source.size();
        while (result.size() < count) {
            result.add(randomEle(source, limit));
        }
        return result;
    }

    /**
     * 创建指定长度的随机索引
     *
     * @param length 长度
     * @return 随机索引
     * @since 5.2.1
     */
    public int[] randomInts(final int length) {
        final int[] range = range(length);
        for (int i = 0; i < length; i++) {
            int random = randomInt(i, length);
            swap(range, i, random);
        }
        return range;
    }

    /**
     * 生成一个数字列表<br>
     * 自动判定正序反序
     *
     * @param includedStart 开始的数字（包含）
     * @param excludedEnd   结束的数字（不包含）
     * @param step          步进
     * @return 数字列表
     */
    public int[] range(int includedStart, int excludedEnd, int step) {
        if (includedStart > excludedEnd) {
            int tmp = includedStart;
            includedStart = excludedEnd;
            excludedEnd = tmp;
        }
        if (step <= 0) {
            step = 1;
        }
        int deviation = excludedEnd - includedStart;
        int length = deviation / step;
        if (deviation % step != 0) {
            length += 1;
        }
        int[] range = new int[length];
        for (int i = 0; i < length; i++) {
            range[i] = includedStart;
            includedStart += step;
        }
        return range;
    }

    /**
     * 生成一个从0开始的数字列表<br>
     *
     * @param excludedEnd 结束的数字（不包含）
     * @return 数字列表
     */
    private int[] range(final int excludedEnd) {
        return range(0, excludedEnd, 1);
    }

    /**
     * 交换数组中两个位置的值
     *
     * @param array  数组
     * @param index1 位置1
     * @param index2 位置2
     * @return 交换后的数组，与传入数组为同一对象
     */
    public int[] swap(@Nullable final int[] array, int index1, int index2) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        final int tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * 获得一个随机的字符串（只包含数字和字符）
     *
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public String randomString(final int length) {
        return randomString(BASE_CHAR_NUMBER, length);
    }

    /**
     * 获得一个随机的字符串（只包含数字和大写字符）
     *
     * @param length 字符串的长度
     * @return 随机字符串
     * @since 4.0.13
     */
    public String randomStringUpper(final int length) {
        return randomString(BASE_CHAR_NUMBER, length).toUpperCase();
    }

    /**
     * 获得一个随机的字符串（只包含数字和小写字母） 并排除指定字符串
     *
     * @param length   字符串的长度
     * @param elemData 要排除的字符串,如：去重容易混淆的字符串，oO0、lL1、q9Q、pP，不区分大小写
     * @return 随机字符串
     */
    public String randomStringWithoutStr(final int length, @Nonnull final String elemData) {
        String baseStr = BASE_CHAR_NUMBER;
        baseStr = removeAll(baseStr, elemData.toLowerCase().toCharArray());
        return randomString(baseStr, length);
    }

    /**
     * 去除字符串中指定的多个字符，如有多个则全部去除
     *
     * @param str   字符串
     * @param chars 字符列表
     * @return 去除后的字符
     */
    public String removeAll(@Nullable final CharSequence str, @Nullable final char... chars) {
        if (null == str) {
            return null;
        }
        if (chars == null || chars.length == 0) {
            return str.toString();
        }
        final int len = str.length();
        if (0 == len) {
            return str.toString();
        }
        final StringBuilder builder = new StringBuilder(len);
        char c;
        for (int i = 0; i < len; i++) {
            c = str.charAt(i);
            if (Arrays.binarySearch(chars, c) < 0) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * 获得一个只包含数字的字符串
     *
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public String randomNumbers(final int length) {
        return randomString(BASE_NUMBER, length);
    }

    /**
     * 获得一个随机的字符串
     *
     * @param baseString 随机字符选取的样本
     * @param length     字符串的长度
     * @return 随机字符串
     */
    public String randomString(final String baseString, int length) {
        if (Strings.isNullOrEmpty(baseString)) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(length);
        if (length < 1) {
            length = 1;
        }
        int baseLength = baseString.length();
        for (int i = 0; i < length; i++) {
            final int number = randomInt(baseLength);
            sb.append(baseString.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 随机数字，数字为0~9单个数字
     *
     * @return 随机数字字符
     * @since 3.1.2
     */
    public char randomNumber() {
        return randomChar(BASE_NUMBER);
    }

    /**
     * 随机字母或数字，小写
     *
     * @return 随机字符
     * @since 3.1.2
     */
    public char randomChar() {
        return randomChar(BASE_CHAR_NUMBER);
    }

    /**
     * 随机字符
     *
     * @param baseString 随机字符选取的样本
     * @return 随机字符
     * @since 3.1.2
     */
    public char randomChar(String baseString) {
        return baseString.charAt(randomInt(baseString.length()));
    }

    /**
     * 生成随机颜色
     *
     * @return 随机颜色
     * @since 4.1.5
     * @deprecated 使用ImgUtil.randomColor()
     */
    @Deprecated
    public Color randomColor() {
        final Random random = getRandom();
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
}
