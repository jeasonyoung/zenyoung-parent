package top.zenyoung.opencv.face;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import top.zenyoung.opencv.Native;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Objects;

/**
 * 人脸识别基类
 *
 * @author yangyong
 */
@Slf4j
public abstract class BaseFaceDetection extends Native implements FaceDetection {

    @Override
    public final int detection(@Nonnull final File input, @Nonnull final File output) {
        //图片加载
        final Mat img = Imgcodecs.imread(input.getAbsolutePath());
        //检测处理
        final MatOfRect faces = detectionHandler(img);
        if (Objects.nonNull(faces)) {
            final Rect[] rects = faces.toArray();
            if (rects != null) {
                //绘制人脸
                for (final Rect rect : rects) {
                    //绘制人脸矩形区域,
                    Imgproc.rectangle(img, rect, new Scalar(0, 200, 0), 3);
                }
                //图片保存
                Imgcodecs.imwrite(output.getAbsolutePath(), img);
                //识别的人脸数量
                return rects.length;
            }
        }
        return 0;
    }

    /**
     * 检测处理器
     *
     * @param img 检测图片
     * @return 检测结果
     */
    protected abstract MatOfRect detectionHandler(@Nonnull final Mat img);
}
