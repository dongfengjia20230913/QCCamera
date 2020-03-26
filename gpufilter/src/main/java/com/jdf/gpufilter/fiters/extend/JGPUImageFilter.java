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

package com.jdf.gpufilter.fiters.extend;

import android.graphics.PointF;
import android.opengl.GLES20;

import com.jdf.common.utils.JLog;
import com.jdf.gpufilter.util.OpenGlUtils;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.LinkedList;

public class JGPUImageFilter {
    protected static final String TAG ="JGPUImageFilter" ;


    public static final String NO_FILTER_VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "}";
    public static final String NO_FILTER_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    protected final LinkedList<Runnable> runOnDraw;
    protected final String vertexShader;
    protected final String fragmentShader;

    protected int glProgId;
    protected int glAttribPosition;
    protected int glUniformTexture;
    protected int glAttribTextureCoordinate;

    protected int outputWidth;
    protected int outputHeight;
    protected boolean isInitialized;
 

    public JGPUImageFilter() {
        this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
    }

    public JGPUImageFilter(final String vertexShader, final String fragmentShader) {
        runOnDraw = new LinkedList<>();
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
    }

    protected final void init() {
        onInit();
        onInitialized();
    }


    public void onInit() {
        //根据传入的作色器字符串，创建，链接，编译对象，返回程序对象id
        glProgId = OpenGlUtils.loadProgram(vertexShader, fragmentShader);
        //获取程序中顶点位置属性引用
        glAttribPosition = GLES20.glGetAttribLocation(glProgId, "position");
        //获取程序中纹理内容属性引用
        glUniformTexture = GLES20.glGetUniformLocation(glProgId, "inputImageTexture");
        //获取程序中顶点纹理坐标属性引用
        glAttribTextureCoordinate = GLES20.glGetAttribLocation(glProgId, "inputTextureCoordinate");
       

        //标明指初始化一次
        isInitialized = true;
    }

    public void onInitialized() {
    }

    public void ifNeedInit() {
        if (!isInitialized) init();
    }

    public final void destroy() {
        isInitialized = false;
        GLES20.glDeleteProgram(glProgId);
        onDestroy();
    }

    public void onDestroy() {
    }

    public void onOutputSizeChanged(final int width, final int height) {
        outputWidth = width;
        outputHeight = height;
    }

    public void onDraw(final int textureId, final FloatBuffer cubeBuffer,
                       final FloatBuffer textureBuffer) {
        JLog.d(TAG,"ondraw:"+textureId+" isInitialized:"+isInitialized);
        if (!isInitialized) {
            return;
        }

        GLES20.glUseProgram(glProgId);
        runPendingOnDrawTasks();

        cubeBuffer.position(0);
        GLES20.glVertexAttribPointer(glAttribPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer);
        GLES20.glEnableVertexAttribArray(glAttribPosition);
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(glAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
                textureBuffer);
        GLES20.glEnableVertexAttribArray(glAttribTextureCoordinate);
        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(getTextureType(), textureId);
            GLES20.glUniform1i(glUniformTexture, 0);
        }
        onDrawArraysPre();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(glAttribPosition);
        GLES20.glDisableVertexAttribArray(glAttribTextureCoordinate);
        GLES20.glBindTexture(getTextureType(), 0);
    }

    public int getTextureType(){
        return GLES20.GL_TEXTURE_2D;
    }

     public void onDrawArraysPre() {
    }

    public void runPendingOnDrawTasks() {
        synchronized (runOnDraw) {
            while (!runOnDraw.isEmpty()) {
                runOnDraw.removeFirst().run();
            }
        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public int getOutputWidth() {
        return outputWidth;
    }

    public int getOutputHeight() {
        return outputHeight;
    }

    public int getProgram() {
        return glProgId;
    }

    public int getAttribPosition() {
        return glAttribPosition;
    }

    public int getAttribTextureCoordinate() {
        return glAttribTextureCoordinate;
    }

    public int getUniformTexture() {
        return glUniformTexture;
    }

     void setInteger(final int location, final int intValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                ifNeedInit();
                GLES20.glUniform1i(location, intValue);
            }
        });
    }

   public  void setFloat(final int location, final float floatValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                ifNeedInit();
                GLES20.glUniform1f(location, floatValue);
            }
        });
    }

     void setFloatVec2(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                ifNeedInit();
                GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

     void setFloatVec3(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                ifNeedInit();
                GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

     void setFloatVec4(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                ifNeedInit();
                GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

     void setFloatArray(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                ifNeedInit();
                GLES20.glUniform1fv(location, arrayValue.length, FloatBuffer.wrap(arrayValue));
            }
        });
    }

     void setPoint(final int location, final PointF point) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                ifNeedInit();
                float[] vec2 = new float[2];
                vec2[0] = point.x;
                vec2[1] = point.y;
                GLES20.glUniform2fv(location, 1, vec2, 0);
            }
        });
    }

     void setUniformMatrix3f(final int location, final float[] matrix) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                ifNeedInit();
                GLES20.glUniformMatrix3fv(location, 1, false, matrix, 0);
            }
        });
    }

     void setUniformMatrix4f(final int location, final float[] matrix) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                ifNeedInit();
                GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0);
            }
        });
    }

     void runOnDraw(final Runnable runnable) {
        synchronized (runOnDraw) {
            runOnDraw.addLast(runnable);
        }
    }


    public static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public void setOESTransformMatrix(float[] transformMatrix){}
}
