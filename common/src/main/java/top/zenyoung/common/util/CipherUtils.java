package top.zenyoung.common.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.Base64Utils;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * AES加密工具类
 *
 * @author yangyong
 * 2020/3/30 10:00 上午
 **/
@Slf4j
public class CipherUtils {
    private static final String AES = "AES";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private static final String RSA = "RSA";
    private static final int RSA_MAX_ENCRYPT_BLOCK = 117;
    private static final int RAS_MAX_DECRYPT_BLOCK = 256;

    @SneakyThrows({GeneralSecurityException.class})
    private static byte[] aesHandler(final int cipherMode, @Nonnull final byte[] raw, @Nonnull final byte[] secret) {
        final SecretKeySpec keySpec = new SecretKeySpec(secret, AES);
        final Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(cipherMode, keySpec);
        return cipher.doFinal(raw);
    }

    public static byte[] aesEncrypt(@Nonnull final byte[] raw, @Nonnull final byte[] secret) {
        return aesHandler(Cipher.ENCRYPT_MODE, raw, secret);
    }

    public static byte[] aesDecrypt(@Nonnull final byte[] raw, @Nonnull final byte[] secret) {
        return aesHandler(Cipher.DECRYPT_MODE, raw, secret);
    }

    @SneakyThrows({IOException.class, GeneralSecurityException.class})
    private static void aesHandler(final int cipherMode, @Nonnull final File raw, @Nonnull final byte[] secret, @Nonnull final byte[] iv) {
        //密文临时文件
        final File destFile = File.createTempFile(raw.getName(), ".tmp", new File(raw.getParent()));
        if (!destFile.isFile()) {
            final boolean ret = destFile.createNewFile();
            if (!ret) {
                throw new RuntimeException("创建临时文件失败!");
            }
        }
        //加密器处理
        final Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        final SecretKeySpec keySpec = new SecretKeySpec(secret, AES);
        final IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(cipherMode, keySpec, ivSpec);
        final RandomAccessFile dest = new RandomAccessFile(destFile, "rw");
        try (final CipherInputStream cin = new CipherInputStream(FileUtils.openInputStream(raw), cipher)) {
            dest.seek(0);
            final byte[] buf = new byte[1024];
            int read;
            while ((read = cin.read(buf, 0, buf.length)) != -1) {
                dest.write(buf, 0, read);
            }
            //关闭写入
            dest.close();
        } catch (Throwable ex) {
            log.error("aesEncrypt(secret: {},iv: {},raw: {})-exp: {}", secret, iv, raw, ex.getMessage());
            throw ex;
        } finally {
            //加密完成覆盖源文件
            FileUtils.copyFile(destFile, raw);
            //删除临时文件
            destFile.deleteOnExit();
        }
    }

    public static void aesEncrypt(@Nonnull final File raw, @Nonnull final byte[] secret, @Nonnull final byte[] iv) {
        aesHandler(Cipher.ENCRYPT_MODE, raw, secret, iv);
    }

    public static void aesDecrypt(@Nonnull final File raw, @Nonnull final byte[] secret, @Nonnull final byte[] iv) {
        aesHandler(Cipher.DECRYPT_MODE, raw, secret, iv);
    }

    @SneakyThrows({GeneralSecurityException.class, IOException.class})
    private static byte[] rsaHandler(final int cipherMode, @Nonnull final byte[] raw, @Nonnull final Key key) {
        final Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(cipherMode, key);
        final int len = raw.length;
        final int max = cipherMode == Cipher.ENCRYPT_MODE ? RSA_MAX_ENCRYPT_BLOCK : RAS_MAX_DECRYPT_BLOCK;
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            int pos = 0, offset;
            byte[] buf;
            while ((offset = len - pos) > 0) {
                if (offset > max) {
                    buf = cipher.doFinal(raw, pos, max);
                } else {
                    buf = cipher.doFinal(raw, pos, offset);
                }
                output.write(buf);
                pos += max;
            }
            return output.toByteArray();
        }
    }

    public static byte[] rsaEncrypt(@Nonnull final byte[] raw, @Nonnull final PublicKey publicKey) {
        return rsaHandler(Cipher.ENCRYPT_MODE, raw, publicKey);
    }

    @SneakyThrows({GeneralSecurityException.class})
    public static byte[] rsaEncrypt(@Nonnull final byte[] raw, @Nonnull final String base64PublicKey) {
        final KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        final PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(Base64Utils.decodeFromString(base64PublicKey)));
        return rsaEncrypt(raw, publicKey);
    }

    public static byte[] rsaDecrypt(@Nonnull final byte[] raw, @Nonnull final PrivateKey privateKey) {
        return rsaHandler(Cipher.DECRYPT_MODE, raw, privateKey);
    }

    @SneakyThrows({GeneralSecurityException.class})
    public static byte[] rsaDecrypt(@Nonnull final byte[] raw, @Nonnull final String base64PrivateKey) {
        final KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        final PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64Utils.decodeFromString(base64PrivateKey)));
        return rsaDecrypt(raw, privateKey);
    }
}
