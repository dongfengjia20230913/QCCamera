package com.jdf.camera.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.jdf.camera.util.ImageUtils;
import com.jdf.common.utils.JLog;

import java.util.Arrays;

//import com.jdf.camera.controller.DetectController;

public class JCamera2Loader extends CameraLoader {

    private static final String TAG = "Camera2Loader";

    private Activity mActivity;

    private CameraManager mCameraManager;
    private CameraCharacteristics mCharacteristics;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private ImageReader mImageReader;

    private String mCameraId;
    private int mCameraFacing = CameraCharacteristics.LENS_FACING_BACK;
    private int mViewWidth;
    private int mViewHeight;
    private float mAspectRatio = 0.75f; // 4:3
    private Size mPreviewSize;
    private Integer mSensorOrientation;
    public JCamera2Loader(Activity activity) {
        mActivity = activity;
        mCameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
    }

    @Override
    public void onResume(int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
        JLog.d("jiadongfeng1", "onResume with WxH[%d, %d]: " , mViewWidth , mViewHeight);
        setUpCamera();
    }

    @Override
    public void onPause() {
        releaseCamera();
    }

    @Override
    public void switchCamera() {
        mCameraFacing ^= 1;
        Log.d(TAG, "current camera facing is: " + mCameraFacing);
        releaseCamera();
        setUpCamera();
    }

    @Override
    public int getCameraOrientation() {
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
        try {
            int size = mCameraManager.getCameraIdList().length;
            return size > 1;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isFrontCamera() {
        return mCameraFacing == CameraCharacteristics.LENS_FACING_FRONT;
    }

    @SuppressLint("MissingPermission")
    private void setUpCamera() {
        try {
            mCameraId = getCameraId(mCameraFacing);
            mCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            setUpCameraOutputs();
            JLog.d(TAG, "Opening camera (ID: " + mCameraId + ") sucess.");
            mCameraManager.openCamera(mCameraId, mCameraDeviceCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Opening camera (ID: " + mCameraId + ") failed.");
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    private String getCameraId(int facing) throws CameraAccessException {
        for (String cameraId : mCameraManager.getCameraIdList()) {
            if (mCameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.LENS_FACING) ==
                    facing) {
                return cameraId;
            }
        }
        // default return
        return Integer.toString(facing);
    }

    private void startCaptureSession() {
        Log.d(TAG, "size: " + mPreviewSize);
        Log.d("jiadongfeng1","Opening camera preview: " + mPreviewSize.getWidth() + "x" + mPreviewSize.getHeight());

        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.YUV_420_888, 2);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                if (reader != null) {
                    Image image = reader.acquireNextImage();

                    if (image != null) {
                        if (mOnPreviewFrameListener != null) {
                            byte[] data = ImageUtils.generateNV21Data(image);
                            mOnPreviewFrameListener.onPreviewFrame(data, image.getWidth(), image.getHeight());
                        }

                        image.close();
                    }
                }
            }
        }, null);

        try {
            mCameraDevice.createCaptureSession(Arrays.asList(mImageReader.getSurface()), mCaptureStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to start camera session");
        }
    }


    private CameraCaptureSession.StateCallback mCaptureStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            if (mCameraDevice == null) {
                return;
            }
            mCaptureSession = session;
            try {
                CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                builder.addTarget(mImageReader.getSurface());
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                session.setRepeatingRequest(builder.build(), null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "Failed to configure capture session.");
        }
    };
    /**
     * set preview size,sesor orientation, and create dection api
     */
    private void setUpCameraOutputs() {
        if (mViewWidth == 0 || mViewHeight == 0) {
            StreamConfigurationMap map = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            mPreviewSize = new Size(0, 0);
        }else {
            StreamConfigurationMap map = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            Size[] sizes = map.getOutputSizes(ImageFormat.YUV_420_888);
            int orientation = getCameraOrientation();
            Log.d("jiadongfeng1", "orientation: " + orientation);

            boolean swapRotation = orientation == 90 || orientation == 270;

            int width = swapRotation ? mViewHeight : mViewWidth;
            int height = swapRotation ? mViewWidth : mViewHeight;
            mPreviewSize = getSuitableSize(sizes, width, height, mAspectRatio);
            Log.d("jiadongfeng1", "width: " + width+" height:"+height);

            Log.d("jiadongfeng1", "orientation: " + orientation+" mViewWidth:"+mViewWidth+" mViewHeight:"+mViewHeight+" mPreviewSize:"+mPreviewSize);

        }

        JLog.d("jiadongfeng1", "get mPreviewSize[%s], mSensorOrientation[%d]",mPreviewSize,mSensorOrientation);


    }

    private Size getSuitableSize(Size[] sizes, int width, int height, float aspectRatio) {
        int minDelta = Integer.MAX_VALUE;
        int index = 0;
        Log.d("jiadongfeng1", "getSuitableSize. aspectRatio: " + aspectRatio);

        for (int i = 0; i < sizes.length; i++) {

            Size size = sizes[i];
            // 先判断比例是否相等
            boolean isRadioSame = size.getWidth() * aspectRatio == size.getHeight();

            if (isRadioSame) {
                int delta = Math.abs(width - size.getWidth());
                Log.d("jiadongfeng1", "getSuitableSize.size " + size+" isRadioSame:" + isRadioSame+" delta:"+delta);
                Log.d("jiadongfeng1", "width:" + width+"  size.getWidth():" +  size.getWidth());

                if (delta == 0) {
                    return size;
                }
                if (minDelta > delta) {
                    minDelta = delta;
                    index = i;
                }
            }
        }
        return sizes[index];
    }

    private CameraDevice.StateCallback mCameraDeviceCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            startCaptureSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    };



}
