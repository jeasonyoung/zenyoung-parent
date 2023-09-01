package top.zenyoung.opencv.face;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * 人脸识别-工厂接口
 *
 * @author yangyong
 */
public interface FaceDetection {
    /**
     * 人脸检测
     *
     * @param input  输入图片文件
     * @param output 输出图片文件
     * @return 检测到的人脸数量
     */
    int detection(@Nonnull final File input, @Nonnull final File output);
}
