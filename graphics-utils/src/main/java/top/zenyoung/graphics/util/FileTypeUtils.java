package top.zenyoung.graphics.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件类型工具
 * <p>此工具根据文件的前几位bytes猜测文件类型，对于文本、zip判断不准确，对于视频、图片类型判断准确</p>
 * <p>需要注意的是，xlsx、docx等Office2007格式，全部识别为zip，因为新版采用了OpenXML格式，这些格式本质上是XML文件打包为zip</p>
 *
 * @author young
 */
public class FileTypeUtils {
    private final static Map<String, String> FILE_TYPE_MAP = new HashMap<String, String>(50) {
        {
            put("ffd8ff", "jpg");
            put("89504e47", "png");
            put("4749463837", "gif");
            put("4749463839", "gif");
            put("49492a00227105008037", "tif"); // TIFF (tif)
            put("424d228c010000000000", "bmp"); // 16色位图(bmp)
            put("424d8240090000000000", "bmp"); // 24色位图(bmp)
            put("424d8e1b030000000000", "bmp"); // 256色位图(bmp)
            put("41433130313500000000", "dwg"); // CAD (dwg)
            put("7b5c727466315c616e73", "rtf"); // Rich Text Format (rtf)
            put("38425053000100000000", "psd"); // Photoshop (psd)
            put("46726f6d3a203d3f6762", "eml"); // Email [Outlook Express 6] (eml)
            put("5374616E64617264204A", "mdb"); // MS Access (mdb)
            put("252150532D41646F6265", "ps");
            put("255044462d312e", "pdf"); // Adobe Acrobat (pdf)
            put("2e524d46000000120001", "rmvb"); // rmvb/rm相同
            put("464c5601050000000900", "flv"); // flv与f4v相同
            put("0000001C66747970", "mp4");
            put("00000020667479706", "mp4");
            put("00000018667479706D70", "mp4");
            put("49443303000000002176", "mp3");
            put("000001ba210001000180", "mpg"); //
            put("3026b2758e66cf11a6d9", "wmv"); // wmv与asf相同
            put("52494646e27807005741", "wav"); // Wave (wav)
            put("52494646d07d60074156", "avi");
            put("4d546864000000060001", "mid"); // MIDI (mid)
            put("526172211a0700cf9073", "rar"); // WinRAR
            put("235468697320636f6e66", "ini");
            put("504B03040a0000000000", "jar");
            put("504B0304140008000800", "jar");
            // MS Excel 注意：word、msi 和 excel的文件头一样
            put("d0cf11e0a1b11ae10", "xls");
            put("504B0304", "zip");
            put("4d5a9000030000000400", "exe"); // 可执行文件
            put("3c25402070616765206c", "jsp"); // jsp文件
            put("4d616e69666573742d56", "mf"); // MF文件
            put("7061636b616765207765", "java"); // java文件
            put("406563686f206f66660d", "bat"); // bat文件
            put("1f8b0800000000000000", "gz"); // gz文件
            put("cafebabe0000002e0041", "class"); // class文件
            put("49545346030000006000", "chm"); // chm文件
            put("04000000010000001300", "mxp"); // mxp文件
            put("6431303a637265617465", "torrent");
            put("6D6F6F76", "mov"); // Quicktime (mov)
            put("FF575043", "wpd"); // WordPerfect (wpd)
            put("CFAD12FEC5FD746F", "dbx"); // Outlook Express (dbx)
            put("2142444E", "pst"); // Outlook (pst)
            put("AC9EBD8F", "qdf"); // Quicken (qdf)
            put("E3828596", "pwl"); // Windows Password (pwl)
            put("2E7261FD", "ram"); // Real Audio (ram)
        }
    };

    /**
     * 根据文件流的头部信息获得文件类型
     *
     * @param fileStreamHexHead 文件流头部16进制字符串
     * @return 文件类型，未找到为{@code null}
     */
    public static String getType(@Nonnull final String fileStreamHexHead) {
        for (Map.Entry<String, String> fileTypeEntry : FILE_TYPE_MAP.entrySet()) {
            if (fileStreamHexHead.equalsIgnoreCase(fileTypeEntry.getKey())) {
                return fileTypeEntry.getValue();
            }
        }
        return null;
    }

    /**
     * 根据文件流的头部信息获得文件类型<br>
     * 注意此方法会读取头部28个bytes，造成此流接下来读取时缺少部分bytes<br>
     * 因此如果想服用此流，流需支持{@link InputStream#reset()}方法。
     *
     * @param in {@link InputStream}
     * @return 类型，文件的扩展名，未找到为{@code null}
     * @throws IOException 读取流引起的异常
     */
    public static String getType(@Nonnull final InputStream in) throws IOException {
        final int len = 28;
        if (in.available() > len) {
            final byte[] buf = new byte[len];
            if (in.read(buf, 0, len) >= len) {
                return getType(Hex.encodeHexString(buf));
            }
        }
        return null;
    }

    /**
     * 根据文件流的头部信息获得文件类型
     * 注意此方法会读取头部28个bytes，造成此流接下来读取时缺少部分bytes<br>
     * 因此如果想服用此流，流需支持{@link InputStream#reset()}方法。
     *
     * <pre>
     *     1、无法识别类型默认按照扩展名识别
     *     2、xls、doc、msi头信息无法区分，按照扩展名区分
     *     3、zip可能为docx、xlsx、pptx、jar、war、ofd头信息无法区分，按照扩展名区分
     * </pre>
     *
     * @param in       {@link InputStream}
     * @param filename 文件名
     * @return 类型，文件的扩展名，未找到为{@code null}
     * @throws IOException 读取流引起的异常
     */
    public static String getType(@Nonnull final InputStream in, @Nonnull final String filename) throws IOException {
        final String extName = FilenameUtils.getExtension(filename);
        String typeName = getType(in);
        if (null == typeName) {
            // 未成功识别类型，扩展名辅助识别
            typeName = extName;
        } else if ("xls".equals(typeName)) {
            // xls、doc、msi的头一样，使用扩展名辅助判断
            if ("doc".equalsIgnoreCase(extName)) {
                typeName = "doc";
            } else if ("msi".equalsIgnoreCase(extName)) {
                typeName = "msi";
            }
        } else if ("zip".equals(typeName)) {
            // zip可能为docx、xlsx、pptx、jar、war、ofd等格式，扩展名辅助判断
            if ("docx".equalsIgnoreCase(extName)) {
                typeName = "docx";
            } else if ("xlsx".equalsIgnoreCase(extName)) {
                typeName = "xlsx";
            } else if ("pptx".equalsIgnoreCase(extName)) {
                typeName = "pptx";
            } else if ("jar".equalsIgnoreCase(extName)) {
                typeName = "jar";
            } else if ("war".equalsIgnoreCase(extName)) {
                typeName = "war";
            } else if ("ofd".equalsIgnoreCase(extName)) {
                typeName = "ofd";
            }
        } else if ("jar".equals(typeName)) {
            // wps编辑过的.xlsx文件与.jar的开头相同,通过扩展名判断
            if ("xlsx".equalsIgnoreCase(extName)) {
                typeName = "xlsx";
            } else if ("docx".equalsIgnoreCase(extName)) {
                // issue#I47JGH
                typeName = "docx";
            }
        }
        return typeName;
    }

    /**
     * 根据文件流的头部信息获得文件类型
     *
     * <pre>
     *     1、无法识别类型默认按照扩展名识别
     *     2、xls、doc、msi头信息无法区分，按照扩展名区分
     *     3、zip可能为docx、xlsx、pptx、jar、war头信息无法区分，按照扩展名区分
     * </pre>
     *
     * @param file 文件 {@link File}
     * @return 类型，文件的扩展名，未找到为{@code null}
     * @throws IOException 读取文件引起的异常
     */
    public static String getType(final File file) throws IOException {
        try (final FileInputStream in = new FileInputStream(file)) {
            return getType(in, file.getName());
        }
    }
}
