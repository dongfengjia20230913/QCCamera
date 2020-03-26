package com.jdf.camera.camera.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.jdf.camera.camera.loader.JCamera2OESLoader;
import com.jdf.camera.ui.CameraV2OESGLSurfaceView;
import com.jdf.common.utils.JLog;
import com.jdf.gpufilter.GPUImage;
import com.jdf.gpufilter.fiters.JGPUImageGrayFilter;
import com.jdf.gpufilter.fiters.extend.JGPUImageFilter;
import com.jdf.gpufilter.fiters.extend.JImageOESFboFilter;
import com.jdf.gpufilter.fiters.extend.JImageOESFilter;
import com.jdf.gpufilter.util.OpenGlUtils;
import com.jdf.gpufilter.util.Rotation;
import com.jdf.gpufilter.util.TextureRotationUtil;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static com.jdf.gpufilter.util.TextureRotationUtil.CUBE;


public class JImageOESFboRender implements GLSurfaceView.Renderer {

    public static final String TAG = "Filter_CameraV2Renderer";
    CameraV2OESGLSurfaceView mCameraV2GLSurfaceView;
    JCamera2OESLoader mCameraLoaer;
    boolean bIsPreviewStarted;
    private int mOESTextureId = -1;
    private SurfaceTexture mSurfaceTexture;
    private float[] transformMatrix = new float[16];
    //输入纹理，是一個FBO滤镜
    private JImageOESFilter mOESInputFilter;

    //输出普通纹理滤镜
    private JGPUImageFilter normalGrayFilter;

    private int outputWidth;
    private int outputHeight;
    private final Queue<Runnable> runOnDraw;

    private  FloatBuffer glCubeBuffer;
    private  FloatBuffer glTextureBuffer;
    private boolean flipHorizontal;
    private boolean flipVertical;
    private Context mContext;


    public JImageOESFboRender(final JImageOESFboFilter filter){
        runOnDraw = new LinkedList<>();
        mOESInputFilter = filter;
        normalGrayFilter = new JGPUImageGrayFilter();
        glCubeBuffer = OpenGlUtils.createBuffer(CUBE);
        glTextureBuffer = OpenGlUtils.createBuffer(TextureRotationUtil.TEXTURE_ROTATED_180);

    }

    public void init(CameraV2OESGLSurfaceView surfaceView, JCamera2OESLoader camera, boolean isPreviewStarted, Context context) {
        mContext = context;
        mCameraV2GLSurfaceView = surfaceView;
        mCameraLoaer = camera;
        bIsPreviewStarted = isPreviewStarted;
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
        JLog.i(TAG, "onSurfaceCreated......");
        mOESTextureId = OpenGlUtils.createOESTextureObject();
        mOESInputFilter.ifNeedInit();
        normalGrayFilter.ifNeedInit();

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        mOESInputFilter.onOutputSizeChanged(width,height);
        JLog.i(TAG, "onSurfaceChanged: " + width + ", " + height);
        normalGrayFilter.onOutputSizeChanged(width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        Long t1 = System.currentTimeMillis();
        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(transformMatrix);
            mOESInputFilter.setOESTransformMatrix(transformMatrix);
        }

        if (!bIsPreviewStarted) {
            bIsPreviewStarted = initSurfaceTexture();
            bIsPreviewStarted = true;
            return;
        }

        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        runAll(runOnDraw);
        Log.i(TAG, "onDrawFrame: time: " + mOESInputFilter +" mOESTextureId:"+mOESTextureId);

        int texture = mOESInputFilter.onDrawFBO(mOESTextureId,glCubeBuffer,glTextureBuffer);
        normalGrayFilter.onDraw(texture,glCubeBuffer,glTextureBuffer);

        long t2 = System.currentTimeMillis();
        long t = t2 - t1;
    }

    public boolean initSurfaceTexture() {
        if (mCameraLoaer == null || mCameraV2GLSurfaceView == null || mCameraLoaer.mCameraDevice == null) {
            JLog.i(TAG, "mCamera or mGLSurfaceView is null!");
            return false;
        }
        mSurfaceTexture = new SurfaceTexture(mOESTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//                mCameraV2GLSurfaceView.requestRender();
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

    public void setFilter(final JImageOESFilter filter) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                final JImageOESFilter oldFilter = mOESInputFilter;
                mOESInputFilter = filter;
                if (oldFilter != null) {
                    oldFilter.destroy();
                }
                JLog.d("jiadongfeng4","oldfilter:"+oldFilter+" newFilter:"+ mOESInputFilter);
                mOESInputFilter.ifNeedInit();
                GLES20.glUseProgram(mOESInputFilter.getProgram());
                mOESInputFilter.onOutputSizeChanged(outputWidth, outputHeight);
            }
        });
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (runOnDraw) {
            runOnDraw.add(runnable);
        }
    }
    private void runAll(Queue<Runnable> queue) {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        }
    }
}
