package com.jdf.camera.camera.loader;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.jdf.camera.util.ImageUtils;
import com.jdf.common.utils.JLog;

import java.util.Arrays;

/**
 * Created by jiadongfeng in 2020-03-25
 */

public class JCamera2OESLoader extends CameraLoader {

    public static final String TAG = "Filter_CameraV2";

    private Activity mActivity;
    public CameraDevice mCameraDevice;
    private String mCameraId;
    public Size mPreviewSize;
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;
    private SurfaceTexture mSurfaceTexture;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mCameraCaptureSession;
    private int mViewWidth;
    private int mViewHeight;
    CameraManager mCameraManager;
    protected int mCameraFacing = CameraCharacteristics.LENS_FACING_BACK;
    protected CameraCharacteristics mCharacteristics;
    private Integer mSensorOrientation;
    protected float mAspectRatio = 0.75f; // 4:3


    public JCamera2OESLoader(Activity activity) {
        mActivity = activity;
        mCameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        startCameraThread();
    }

    public void startCameraThread() {
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }

    public void onResume(int width, int height) {
        JLog.d(TAG, "onResume[%d,%d]...", width, height);
        mViewWidth = width;
        mViewHeight = height;
        setUpCamera();
    }


    protected void setUpCamera() {
        try {
            //获取屏幕方向，相机传感器方向，和预览大小
            setUpCameraOutputs();
            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                JLog.e(TAG, "Dont have CAMERA permission");
                return;
            }
            mCameraManager.openCamera(mCameraId, mCameraDeviceCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            JLog.e(TAG, "Opening camera (ID: " + mCameraId + ") failed.");
            e.printStackTrace();
        }
    }

    /**
     * set preview size,sesor orientation, and create dection api
     */
    protected void setUpCameraOutputs() throws CameraAccessException {
        mCameraId = getCameraId(mCameraFacing);
        mCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
        if (mViewWidth == 0 || mViewHeight == 0) {
            JLog.e(TAG, "view width or height is 0!");
            mSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            mPreviewSize = new Size(0, 0);
        } else {
            // 获取到相机传感器的方向
            mSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            StreamConfigurationMap map = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            Size[] sizes = map.getOutputSizes(ImageFormat.YUV_420_888);
            int orientation = getCameraOrientation();
            boolean swapRotation = orientation == 90 || orientation == 270;
            int width = swapRotation ? mViewHeight : mViewWidth;
            int height = swapRotation ? mViewWidth : mViewHeight;
            mPreviewSize = ClassLoaderUtil.getSuitableSize(sizes, width, height, mAspectRatio);
            JLog.d(TAG, "mSensorOrientation[%d],cameraOritation[%d],mPreviewSize[%s]", mSensorOrientation, orientation, mPreviewSize.toString());
        }
    }


    public CameraDevice.StateCallback mCameraDeviceCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            JLog.d(TAG, "Camera onOpene...");
            mCameraDevice = camera;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            JLog.d(TAG, "Camera onDisconnected...");

            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            JLog.d(TAG, "Camera onError[%d]...", error);
            camera.close();
            mCameraDevice = null;
        }
    };


    public void setPreviewTexture(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
    }

    public void startPreview() {
        final ImageReader imageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.YUV_420_888, 2);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                if (reader != null) {
                    Image image = reader.acquireNextImage();
                    if (ImageUtils.saveBitmap) {
                        Bitmap bitmap = ImageUtils.ImageToBitmap(image);
                        if (bitmap != null) {
                            ImageUtils.saveBitmap = false;
                            ImageUtils.saveBitmap(bitmap);
                        }

                    }
                    Log.d("jiadongfeng3", "onImageAvailable: " + image.getWidth() + "x" + image.getHeight());
                    if (image != null) {
                        image.close();
                    }
                }
            }
        }, mCameraHandler);
        final Surface imageReaderSurface = imageReader.getSurface();

        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        final Surface surface = new Surface(mSurfaceTexture);

        try {

            mCameraDevice.createCaptureSession(Arrays.asList(imageReaderSurface, surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        builder.addTarget(imageReaderSurface);
                        builder.addTarget(surface);
                        mCaptureRequest = builder.build();
                        mCameraCaptureSession = session;
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPause() {
        releaseCamera();
    }

    protected void releaseCamera() {
        //TODO
    }

    @Override
    public void switchCamera() {
        mCameraFacing ^= 1;
        JLog.d(TAG, "switch camera facing: " + mCameraFacing);
        releaseCamera();
        setUpCamera();
    }

    protected String getCameraId(int facing) throws CameraAccessException {
        for (String cameraId : mCameraManager.getCameraIdList()) {
            if (mCameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.LENS_FACING) ==
                    facing) {
                return cameraId;
            }
        }
        // default return
        return Integer.toString(facing);
    }

    public int getCameraOrientation() {
        //屏幕旋转方向
        int degrees = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        switch (degrees) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                degrees = 0;
                break;
        }
        int orientation = 0;
        try {
            String cameraId = getCameraId(mCameraFacing);
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
            orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "degrees: " + degrees + ", orientation: " + orientation + ", mCameraFacing: " + mCameraFacing);
        if (mCameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
            return (orientation + degrees) % 360;
        } else {
            return (orientation - degrees) % 360;
        }
    }

    @Override
    public boolean hasMultipleCamera() {
        return false;
    }

    @Override
    public boolean isFrontCamera() {
        return false;
    }


}
