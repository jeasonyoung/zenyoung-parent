package top.zenyoung.common.util;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

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
import java.util.List;

/**
 * AES加密工具类
 *
 * @author yangyong
 * 2020/3/30 10:00 上午
 **/
@Slf4j
public class CipherUtils {
    private static final String AES = "AES";
    private static final String AES_CBC = "AES/CBC/PKCS5Padding";
    private static final String AES_ECB = "AES/ECB/PKCS5Padding";

    private static final String RSA = "RSA";
    private static final String RSA_ECB = "RSA/ECB/PKCS1Padding";
    private static final int RSA_KEY_SIZE = 1024;
    private static final int RSA_RESERVE_BYTES = 11;
    private static final int RAS_MAX_DECRYPT_BLOCK = RSA_KEY_SIZE / 8;
    private static final int RSA_MAX_ENCRYPT_BLOCK = RAS_MAX_DECRYPT_BLOCK - RSA_RESERVE_BYTES;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @SneakyThrows({GeneralSecurityException.class})
    private static byte[] aesHandler(final int cipherMode, @Nonnull final byte[] raw, @Nonnull final byte[] secret) {
        final SecretKeySpec keySpec = new SecretKeySpec(secret, AES);
        final Cipher cipher = Cipher.getInstance(AES_ECB);
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
        final Cipher cipher = Cipher.getInstance(AES_CBC);
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
    private static byte[] rsaHandler(final int cipherMode, @Nonnull final byte[] data, @Nonnull final Key key) {
        final Cipher cipher = Cipher.getInstance(RSA_ECB);
        cipher.init(cipherMode, key);
        final int len = data.length;
        final int block = (cipherMode == Cipher.ENCRYPT_MODE) ? RSA_MAX_ENCRYPT_BLOCK : RAS_MAX_DECRYPT_BLOCK;
        final int blockCount = (len / block) + (len % block == 0 ? 0 : 1);
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream(blockCount * block)) {
            for (int offset = 0; offset < len; offset += block) {
                int input = (len - offset);
                if (input > block) {
                    input = block;
                }
                final byte[] buf = cipher.doFinal(data, offset, input);
                output.write(buf);
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
        final byte[] keys = Base64.decodeBase64(base64PublicKey);
        final PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keys));
        return rsaEncrypt(raw, publicKey);
    }

    public static byte[] rsaDecrypt(@Nonnull final byte[] raw, @Nonnull final PrivateKey privateKey) {
        return rsaHandler(Cipher.DECRYPT_MODE, raw, privateKey);
    }

    @SneakyThrows({GeneralSecurityException.class})
    public static byte[] rsaDecrypt(@Nonnull final byte[] raw, @Nonnull final String base64PrivateKey) {
        final KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        final byte[] keys = Base64.decodeBase64(base64PrivateKey);
        final PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keys));
        return rsaDecrypt(raw, privateKey);
    }

    @SneakyThrows({GeneralSecurityException.class})
    public static List<String> generateRsaKey(final int keySize) {
        final List<String> keys = Lists.newArrayList();
        final KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA);
        generator.initialize(Math.max(keySize, RSA_KEY_SIZE));
        final KeyPair keyPair = generator.generateKeyPair();
        //公钥
        keys.add(Base64.encodeBase64String(keyPair.getPublic().getEncoded()));
        //私钥
        keys.add(Base64.encodeBase64String(keyPair.getPrivate().getEncoded()));
        //返回数据
        return keys;
    }
}
