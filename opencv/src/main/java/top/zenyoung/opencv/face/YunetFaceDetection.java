package top.zenyoung.opencv.face;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.objdetect.FaceDetectorYN;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Yunet 检测模型
 *
 * @author young
 */
@Slf4j
public class YunetFaceDetection extends BaseFaceDetection {
    private final FaceDetectorYN model;

    private YunetFaceDetection() throws IOException {
        final int backendId = Dnn.DNN_BACKEND_OPENCV, targetId = Dnn.DNN_TARGET_CPU, topK = 5000;
        final Size inputSize = new Size(320, 320);
        final float confThreshold = 0.6f, nmsThreshold = 0.3f;
        //模型文件
        final File modelFile = copyResourceToTemp("data/faceDetection/face_detection_yunet_2023mar.onnx");
        this.model = FaceDetectorYN.create(modelFile.getAbsolutePath(), "", inputSize,
                confThreshold, nmsThreshold, topK, backendId, targetId);
    }

    private static YunetFaceDetection instance = null;

    public static YunetFaceDetection getInstance() {
        if (instance == null) {
            try {
                instance = new YunetFaceDetection();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        return instance;
    }

    public void setInputSize(@Nonnull final Size inputSize) {
        this.model.setInputSize(inputSize);
    }

    @Override
    protected MatOfRect detectionHandler(@Nonnull final Mat img) {
        setInputSize(img.size());
        final Mat faceMats = new Mat();
        this.model.detect(img, faceMats);
        final int faces = faceMats.rows();
        final List<Rect> rects = Lists.newArrayList();
        for (int i = 0; i < faces; i++) {
            final int x = (int) (faceMats.get(i, 0)[0]);
            final int y = (int) (faceMats.get(i, 1)[0]);
            final int w = (int) (faceMats.get(i, 2)[0]);
            final int h = (int) (faceMats.get(i, 3)[0]);
            rects.add(new Rect(x, y, w, h));
        }
        return new MatOfRect(rects.toArray(new Rect[0]));
    }
}
