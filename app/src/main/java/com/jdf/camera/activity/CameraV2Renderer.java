package com.jdf.camera.activity;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.jdf.camera.camera.Camera2OESLoader;
import com.jdf.camera.ui.CameraV2GLSurfaceView;
import com.jdf.gpufilter.GPUImage;
import com.jdf.gpufilter.util.Rotation;
import com.jdf.gpufilter.util.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static com.jdf.gpufilter.util.TextureRotationUtil.CUBE;
import static com.jdf.gpufilter.util.TextureRotationUtil.TEXTURE_NO_ROTATION;

/**
 * Created by lb6905 on 2017/7/19.
 */

public class CameraV2Renderer implements GLSurfaceView.Renderer {

    public static final String TAG = "Filter_CameraV2Renderer";
    private Context mContext;
    CameraV2GLSurfaceView mCameraV2GLSurfaceView;
    Camera2OESLoader mCameraLoaer;
    boolean bIsPreviewStarted;
    private int mOESTextureId = -1;
    private SurfaceTexture mSurfaceTexture;
    private float[] transformMatrix = new float[16];
    private FilterEngine mFilterEngine;
    private FloatBuffer mDataBuffer;
    private int mShaderProgram = -1;
    private int aPositionLocation = -1;
    private int aTextureCoordLocation = -1;
    private int uTextureMatrixLocation = -1;
    private int uTextureSamplerLocation = -1;
    private int outputWidth;
    private int outputHeight;

    private  FloatBuffer glCubeBuffer;
    private  FloatBuffer glTextureBuffer;
    private boolean flipHorizontal;
    private boolean flipVertical;


    public void init(CameraV2GLSurfaceView surfaceView, Camera2OESLoader camera, boolean isPreviewStarted, Context context) {
        mContext = context;
        mCameraV2GLSurfaceView = surfaceView;
        mCameraLoaer = camera;
        bIsPreviewStarted = isPreviewStarted;

        glCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        glCubeBuffer.put(CUBE).position(0);

        glTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
//        setRotation(Rotation.NORMAL, false, false);
    }

    public void setRotation(final Rotation rotation,
                            final boolean flipHorizontal, final boolean flipVertical) {
        this.flipHorizontal = flipHorizontal;
        this.flipVertical = flipVertical;
        setRotation(rotation);
    }

    public void setRotation(final Rotation rotation) {
        this.rotation = rotation;
        adjustImageScaling();
    }



    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mOESTextureId = Utils.createOESTextureObject();
        mFilterEngine = new FilterEngine(mOESTextureId, mContext);

        mDataBuffer = glCubeBuffer;
        mShaderProgram = mFilterEngine.getShaderProgram();

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        mFilterEngine.onOutputSizeChanged(width,height);

        Log.i(TAG, "onSurfaceChanged: " + width + ", " + height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Long t1 = System.currentTimeMillis();
        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(transformMatrix);
        }

        if (!bIsPreviewStarted) {
            bIsPreviewStarted = initSurfaceTexture();
            bIsPreviewStarted = true;
            return;
        }

        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        mFilterEngine.drawTexture(transformMatrix);

        long t2 = System.currentTimeMillis();
        long t = t2 - t1;
        Log.i(TAG, "onDrawFrame: time: " + t);
    }

    public boolean initSurfaceTexture() {
        if (mCameraLoaer == null || mCameraV2GLSurfaceView == null) {
            Log.i(TAG, "mCamera or mGLSurfaceView is null!");
            return false;
        }
        mSurfaceTexture = new SurfaceTexture(mOESTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                Log.i("jiadongfeng", "mCamera or mGLSurfaceView is null!");

                mCameraV2GLSurfaceView.requestRender();
            }
        });
        mCameraLoaer.setPreviewTexture(mSurfaceTexture);
        mCameraLoaer.startPreview();
        return true;
    }
    private Rotation rotation = Rotation.ROTATION_90;
   GPUImage.ScaleType scaleType  = GPUImage.ScaleType.CENTER_CROP;
    private void adjustImageScaling() {
        //返回的surface大小，也就是glsurfaceview的大小
        float outputWidth = this.outputWidth;
        float outputHeight = this.outputHeight;
        if (rotation == Rotation.ROTATION_270 || rotation == Rotation.ROTATION_90) {
            outputWidth = this.outputHeight;
            outputHeight = this.outputWidth;
        }

        //根据view的大小和实际要显示的图片大小，获取调整比例
        int imageWidth = mCameraLoaer.mPreviewSize.getWidth();
        int imageHeight = mCameraLoaer.mPreviewSize.getWidth();

        float ratio1 = outputWidth / imageWidth;
        float ratio2 = outputHeight / imageHeight;
        float ratioMax = Math.max(ratio1, ratio2);

        //获取实际要显示的图片大小
        int imageWidthNew = Math.round(imageWidth * ratioMax);
        int imageHeightNew = Math.round(imageHeight * ratioMax);

        //计算要显示的view调整的比例
        float ratioWidth = imageWidthNew / outputWidth;
        float ratioHeight = imageHeightNew / outputHeight;

        float[] cube = CUBE;
        /**
         * 根据旋转角度，重新获取纹理坐标和顶点坐标
         */

        float[] textureCords = TextureRotationUtil.getRotation(rotation, false, false);
        if (scaleType == GPUImage.ScaleType.CENTER_CROP) {
            float distHorizontal = (1 - 1 / ratioWidth) / 2;
            float distVertical = (1 - 1 / ratioHeight) / 2;
            textureCords = new float[]{
                    addDistance(textureCords[0], distHorizontal), addDistance(textureCords[1], distVertical),
                    addDistance(textureCords[2], distHorizontal), addDistance(textureCords[3], distVertical),
                    addDistance(textureCords[4], distHorizontal), addDistance(textureCords[5], distVertical),
                    addDistance(textureCords[6], distHorizontal), addDistance(textureCords[7], distVertical),
            };
        } else {
            cube = new float[]{
                    CUBE[0] / ratioHeight, CUBE[1] / ratioWidth,
                    CUBE[2] / ratioHeight, CUBE[3] / ratioWidth,
                    CUBE[4] / ratioHeight, CUBE[5] / ratioWidth,
                    CUBE[6] / ratioHeight, CUBE[7] / ratioWidth,
            };
        }

        glCubeBuffer.clear();
        glCubeBuffer.put(cube).position(0);
        glTextureBuffer.clear();
        glTextureBuffer.put(textureCords).position(0);
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }
}
