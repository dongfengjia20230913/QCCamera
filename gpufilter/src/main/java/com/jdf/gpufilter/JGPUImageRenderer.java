/*
 * Copyright (C) 2018 CyberAgent, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jdf.gpufilter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.jdf.common.utils.JLog;
import com.jdf.gpufilter.fiters.JGPUImageFilter;
import com.jdf.gpufilter.util.OpenGlUtils;
import com.jdf.gpufilter.util.Rotation;
import com.jdf.gpufilter.util.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class JGPUImageRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "GPUImageRenderer";
    private static final int NO_IMAGE = -1;

    private int glTextureId = NO_IMAGE;


    private final FloatBuffer glCubeBuffer;
    private final FloatBuffer glTextureBuffer;

    private int outputWidth;
    private int outputHeight;

    private Rotation rotation = Rotation.NORMAL;
    private boolean flipHorizontal;
    private boolean flipVertical;


    private final Queue<Runnable> runOnDraw;
    private final Queue<Runnable> runOnDrawEnd;

    private int imageWidth;
    private int imageHeight;

    private int addedPadding;

    private JGPUImageFilter filter;


    private GPUImage.ScaleType scaleType = GPUImage.ScaleType.CENTER_CROP;


    private IntBuffer glRgbBuffer;


    //顶点坐标
    public static final float CUBE[] = {
            -1.0f, -1.0f,//v0
            1.0f, -1.0f,//v1
            -1.0f, 1.0f,//v2
            1.0f, 1.0f,//v3
    };
    //纹理坐标
    public static final float TEXTURE_NO_ROTATION[] = {
            0.0f, 1.0f,//t0
            1.0f, 1.0f,//t1
            0.0f, 0.0f,//t2
            1.0f, 0.0f,//t3
    };

    public JGPUImageRenderer(final JGPUImageFilter filter) {
        this.filter = filter;
        runOnDraw = new LinkedList<>();
        runOnDrawEnd = new LinkedList<>();

        //为顶点坐标添加分配buffer对象
        glCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        glCubeBuffer.put(CUBE).position(0);
        //为纹理坐标添加buffer对象
        glTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
    }



    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(0, 0, 0, 1);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            filter.ifNeedInit();
    }



    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //surface显示大小
        outputWidth = width;
        outputHeight = height;
        ///设置视窗大小及位置
        GLES20.glViewport(0, 0, width, height);
        //使用已创建的程序
        GLES20.glUseProgram(filter.getProgram());
        //向gpu处理对象中传入surface大小
        filter.onOutputSizeChanged(width, height);
        //根据surface大小，缩放图片，使得图片和GlSurfaceView显示大小一致
        adjustImageScaling();
    }

    @Override
    public void onDrawFrame(final GL10 gl) {
        JLog.d("jiadongfeng4","onDraw.....");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        //调用队列任务
        runAll(runOnDraw);
        filter.onDraw(glTextureId, glCubeBuffer, glTextureBuffer);
    }

    private void runAll(Queue<Runnable> queue) {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        }
    }

    private void adjustImageScaling() {
        //返回的surface大小，也就是glsurfaceview的大小
        float outputWidth = this.outputWidth;
        float outputHeight = this.outputHeight;
        if (rotation == Rotation.ROTATION_270 || rotation == Rotation.ROTATION_90) {
            outputWidth = this.outputHeight;
            outputHeight = this.outputWidth;
        }

        //根据view的大小和实际要显示的图片大小，获取调整比例
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

        float[] textureCords = TextureRotationUtil.getRotation(rotation, flipHorizontal, flipVertical);
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

    public void setImageBitmap(final Bitmap bitmap) {
        setImageBitmap(bitmap, true);
    }

    public void setImageBitmap(final Bitmap bitmap, final boolean recycle) {
        if (bitmap == null) {
            return;
        }

        runOnDraw(new Runnable() {

            @Override
            public void run() {
                Bitmap resizedBitmap = null;
                //求余数，OPENGL处理的图片大小必须是2的整数次方
                if (bitmap.getWidth() % 2 == 1) {
                    resizedBitmap = Bitmap.createBitmap(bitmap.getWidth() + 1, bitmap.getHeight(),
                            Bitmap.Config.ARGB_8888);
                    Canvas can = new Canvas(resizedBitmap);
                    can.drawARGB(0x00, 0x00, 0x00, 0x00);
                    can.drawBitmap(bitmap, 0, 0, null);
                    addedPadding = 1;
                } else {
                    addedPadding = 0;
                }

                glTextureId = OpenGlUtils.loadTexture(
                        resizedBitmap != null ? resizedBitmap : bitmap, glTextureId, recycle);
                if (resizedBitmap != null) {
                    resizedBitmap.recycle();
                }
                imageWidth = bitmap.getWidth();
                imageHeight = bitmap.getHeight();
                adjustImageScaling();
            }
        });
    }

    public void setFilter(final JGPUImageFilter filter) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                final JGPUImageFilter oldFilter = JGPUImageRenderer.this.filter;
                JGPUImageRenderer.this.filter = filter;
                if (oldFilter != null) {
                    oldFilter.destroy();//destory
                }
                JGPUImageRenderer.this.filter.ifNeedInit();
                GLES20.glUseProgram(JGPUImageRenderer.this.filter.getProgram());
                JGPUImageRenderer.this.filter.onOutputSizeChanged(outputWidth, outputHeight);
            }
        });
    }


    public void onPreviewFrame(final byte[] data, final int width, final int height) {
        if (glRgbBuffer == null) {
            glRgbBuffer = IntBuffer.allocate(width * height);
        }
        if (runOnDraw.isEmpty()) {
            runOnDraw(new Runnable() {
                @Override
                public void run() {
                    GPUImageNativeLibrary.YUVtoRBGA(data, width, height, glRgbBuffer.array());
                    glTextureId = OpenGlUtils.loadTexture(glRgbBuffer, width, height, glTextureId);

                    if (imageWidth != width) {
                        imageWidth = width;
                        imageHeight = height;
                        adjustImageScaling();
                    }
                }
            });
        }
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (runOnDraw) {
            runOnDraw.add(runnable);
        }
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



}
