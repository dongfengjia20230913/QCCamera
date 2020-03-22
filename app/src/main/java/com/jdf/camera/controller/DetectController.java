package com.jdf.camera.controller;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.Toast;

import com.jdf.camera.R;
import com.jdf.common.utils.JLog;
import com.jdf.common.utils.ScreenUtil;
import com.jdf.common.widget.OverlayView;
import com.jdf.tf.object_detect.Classifier;
import com.jdf.tf.object_detect.TFLiteObjectDetectionAPIModel;
import com.jdf.tf.object_detect.tracking.BorderedText;
import com.jdf.tf.object_detect.tracking.ImageUtils;
import com.jdf.tf.object_detect.tracking.MultiBoxTracker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class DetectController {
    private static final String TAG = "DetectController";

    // Configuration values for the prepackaged SSD model.
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final boolean MAINTAIN_ASPECT = false;
    public static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    public static  boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;
    OverlayView trackingOverlay;
    private Integer sensorOrientation;

    private Classifier detector;

    int previewWidth;
    int previewHeight;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private int[] rgbBytes = null;


    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private BorderedText borderedText;

    private Activity mContext;

    private boolean isProcessingFrame = false;

    private byte[][] yuvBytes = new byte[3][];

    private int yRowStride;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;

    private Handler mHandler;

    public DetectController(Activity context) {
        mContext = context;
        mHandler = new Handler();
    }

    public void onPreviewSizeChosen(final Size size, final int rotation) {
        JLog.d("jiadongfeng1", "onPreviewSizeChosen:" + size + " rotation:" + rotation);

        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, mContext.getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(mContext);

        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            mContext.getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            JLog.e(TAG, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            mContext, "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
        }

        JLog.i(TAG, "Create detector sucess...");


         previewWidth = size.getWidth();
         previewHeight = size.getHeight();

        sensorOrientation = rotation - ScreenUtil.getScreenOrientation((Activity) mContext);
        JLog.i(TAG, "Camera orientation relative to screen canvas: %d", sensorOrientation);

        JLog.i(TAG, "Initializing at size %d x %d", previewWidth, previewHeight);
        //预览帧数据
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) mContext.findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas);
                        boolean debug = false;
                        if (debug) {
                            tracker.drawDebug(canvas);
                        }
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }

    public void onImageAvailable(final Image image) {
        JLog.d("jiadongfeng", "onImageAvailable....." + previewWidth + "---" + previewHeight + "---" + image);
        // We need wait until we have some size from onPreviewSizeChosen
        if (previewWidth == 0 || previewHeight == 0) {
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[previewWidth * previewHeight];
        }
        try {
            if (image == null) {
                return;
            }
            JLog.d("jiadongfeng", "isProcessingFrame:" + isProcessingFrame);

            if (isProcessingFrame) {
                image.close();
                return;
            }
            isProcessingFrame = true;
            JLog.d("jiadongfeng", "set isProcessingFrame:" + isProcessingFrame);

            Trace.beginSection("imageAvailable");
            //Image为图片数据，image.getPlanes()指的是获取该图片的像素矩阵，返回值为一个Plane[]矩阵
            final Image.Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();
            JLog.d("jiadongfeng", "5555555555555:" + isProcessingFrame);

            imageConverter =
                    new Runnable() {
                        @Override
                        public void run() {
                            ImageUtils.convertYUV420ToARGB8888(
                                    yuvBytes[0],
                                    yuvBytes[1],
                                    yuvBytes[2],
                                    previewWidth,
                                    previewHeight,
                                    yRowStride,
                                    uvRowStride,
                                    uvPixelStride,
                                    rgbBytes);
                        }
                    };

            postInferenceCallback =
                    new Runnable() {
                        @Override
                        public void run() {
                            image.close();
                            isProcessingFrame = false;
                        }
                    };
            JLog.d("jiadongfeng", "processImage......:");

            processImage();
        } catch (final Exception e) {
            e.printStackTrace();
            JLog.e("jiadongfeng", "Exception:" + e.getMessage());
            Trace.endSection();
            return;
        }
        Trace.endSection();
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API;
    }

    protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        JLog.d("jiadongfeng2", "fillBytes ......");

        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();

            Log.d("jiadongfeng2", "yuvBytes[i] :" +i+"---"+ yuvBytes[i] + "---" + buffer.capacity());

            if (yuvBytes[i] == null) {
                JLog.d(TAG, "Initializing buffer %d at size %d", i, buffer.capacity());
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            Log.d("jiadongfeng2", "yuvBytes[i] :" +i+"---"+ yuvBytes[i].length + "---" + buffer.capacity());

            try{
                buffer.get(yuvBytes[i]);
            }catch (Exception e){
                e.printStackTrace();;
            }
        }
    }

    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();
        JLog.d("jiadongfeng", "computingDetection:" + computingDetection);

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        JLog.i(TAG, "Preparing image " + currTimestamp + " for detection in bg thread.");

        int[] rgbBytes = getRgbBytes();
        Log.d("jiadongfeng", "rgbBytes:" + rgbBytes.length);

        rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(rgbFrameBitmap);
            SAVE_PREVIEW_BITMAP = false;
        }

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        JLog.i(TAG, "Running detection on image " + currTimestamp);
                        final long startTime = SystemClock.uptimeMillis();
                        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                        final Canvas canvas = new Canvas(cropCopyBitmap);
                        final Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(2.0f);

                        float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                        switch (MODE) {
                            case TF_OD_API:
                                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                                break;
                        }

                        final List<Classifier.Recognition> mappedRecognitions =
                                new LinkedList<Classifier.Recognition>();

                        for (final Classifier.Recognition result : results) {
                            Log.d("jiadongfeng", "-----------result:" + result);
                            final RectF location = result.getLocation();
                            if (location != null && result.getConfidence() >= minimumConfidence) {
                                canvas.drawRect(location, paint);

                                cropToFrameTransform.mapRect(location);

                                result.setLocation(location);
                                mappedRecognitions.add(result);
                            }
                        }

                        tracker.trackResults(mappedRecognitions, currTimestamp);
                        trackingOverlay.postInvalidate();

                        computingDetection = false;

                        mContext.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        JLog.d(TAG, previewWidth + "x" + previewHeight);
                                        JLog.d(TAG, cropCopyBitmap.getWidth() + "x" + cropCopyBitmap.getHeight());
                                        JLog.d(TAG, lastProcessingTimeMs + "ms");
                                    }
                                });
                    }
                });
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (mHandler != null) {
            mHandler.post(r);
        }
    }

    protected int[] getRgbBytes() {
        imageConverter.run();
        return rgbBytes;
    }


    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }


}
