package top.zenyoung.common.util;

import lombok.experimental.UtilityClass;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Base62编码工具
 *
 * @author young
 */
@UtilityClass
public class Base62Utils {
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = ALPHABET.length();

    public String encode(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        BigInteger number = new BigInteger(1, bytes);
        //
        while (number.compareTo(BigInteger.ZERO) > 0) {
            final BigInteger[] quotientAndRemainder = number.divideAndRemainder(BigInteger.valueOf(BASE));
            number = quotientAndRemainder[0];
            int remainder = quotientAndRemainder[1].intValue();
            sb.insert(0, ALPHABET.charAt(remainder));
        }
        // Handle the case where the input is 0
        if (sb.isEmpty()) {
            sb.append(ALPHABET.charAt(0));
        }
        return sb.toString();
    }

    public byte[] decode(final String input) {
        BigInteger number = BigInteger.ZERO;
        for (int i = 0; i < input.length(); i++) {
            int charValue = ALPHABET.indexOf(input.charAt(i));
            if (charValue < 0) {
                throw new IllegalArgumentException("Invalid character in input");
            }
            number = number.multiply(BigInteger.valueOf(BASE)).add(BigInteger.valueOf(charValue));
        }
        byte[] bytes = number.toByteArray();
        if (bytes[0] == 0) {
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return bytes;
    }
}
