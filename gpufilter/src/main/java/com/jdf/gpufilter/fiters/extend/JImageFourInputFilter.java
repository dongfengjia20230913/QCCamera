package com.jdf.gpufilter.fiters.extend;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.jdf.gpufilter.fiters.GPUImageFilter;
import com.jdf.gpufilter.util.OpenGlUtils;
import com.jdf.gpufilter.util.Rotation;
import com.jdf.gpufilter.util.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class JImageFourInputFilter extends GPUImageFilter {
    private static final String VERTEX_SHADER = "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "attribute vec4 inputTextureCoordinate1;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec2 textureCoordinate1;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "    textureCoordinate1 = inputTextureCoordinate1.xy;\n" +
            "}";

    //其他圖片的纹理坐标
    private int filterSecondTextureCoordinateAttribute;

     //片元著色器中的全局變量
    private int filterInputTextureUniform1;
    private int blowoutTextureUniform;
    private int mapTextureUniform;
    private int overlayTextureUniform;

    //圖片內容對應的紋理id
    private int filterSourceTexture1 = OpenGlUtils.NO_TEXTURE;
    private int blowoutTexturId = OpenGlUtils.NO_TEXTURE;
    private int mapTextureId = OpenGlUtils.NO_TEXTURE;
    private int overlayTextureId = OpenGlUtils.NO_TEXTURE;

    private ByteBuffer texture2CoordinatesBuffer;
    private ByteBuffer texture3CoordinatesBuffer;
    private ByteBuffer texture4CoordinatesBuffer;
    private ByteBuffer texture5CoordinatesBuffer;


    private Bitmap bitmap1,bitmap2,bitmap3,bitmap4;

    public JImageFourInputFilter(String fragmentShader) {
        this(VERTEX_SHADER, fragmentShader);
    }

    public JImageFourInputFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        setRotation(Rotation.NORMAL, false, false);
    }

    @Override
    public void onInit() {
        super.onInit();

        filterSecondTextureCoordinateAttribute = GLES20.glGetAttribLocation(getProgram(), "inputTextureCoordinate1");
        //片元着色器里面的全局变量
        filterInputTextureUniform1 = GLES20.glGetUniformLocation(getProgram(), "inputImageTexture1"); // This does assume a name of "inputImageTexture2" for second input texture in the fragment shader

        blowoutTextureUniform = GLES20.glGetUniformLocation(getProgram(), "blowoutTexture"); // This does assume a name of "inputImageTexture2" for second input texture in the fragment shader
        mapTextureUniform = GLES20.glGetUniformLocation(getProgram(), "mapTexture"); // This does assume a name of "inputImageTexture2" for second input texture in the fragment shader
        overlayTextureUniform = GLES20.glGetUniformLocation(getProgram(), "overlayTexture"); // This does assume a name of "inputImageTexture2" for second input texture in the fragment shader

        GLES20.glEnableVertexAttribArray(filterSecondTextureCoordinateAttribute);
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        if (bitmap1 != null && !bitmap1.isRecycled()) {
            setBitmap1(bitmap1);
        }
    }

    public void setBitmap1(final Bitmap bitmap) {
        if (bitmap != null && bitmap.isRecycled()) {
            return;
        }
        this.bitmap1 = bitmap;
        if (this.bitmap1 == null) {
            return;
        }
        runOnDraw(new Runnable() {
            public void run() {
                if (filterSourceTexture1 == OpenGlUtils.NO_TEXTURE) {
                    if (bitmap == null || bitmap.isRecycled()) {
                        return;
                    }
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
                    filterSourceTexture1 = OpenGlUtils.loadTexture(bitmap, OpenGlUtils.NO_TEXTURE, false);
                }
            }
        });
    }

    public void setBlowoutTexture(final Bitmap bitmap) {
        if (bitmap != null && bitmap.isRecycled()) {
            return;
        }
        this.bitmap2 = bitmap;
        if (this.bitmap2 == null) {
            return;
        }
        runOnDraw(new Runnable() {
            public void run() {
                if (blowoutTexturId == OpenGlUtils.NO_TEXTURE) {
                    if (bitmap == null || bitmap.isRecycled()) {
                        return;
                    }
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
                    blowoutTexturId = OpenGlUtils.loadTexture(bitmap, OpenGlUtils.NO_TEXTURE, false);
                }
            }
        });
    }

    public void setMapTexture(final Bitmap bitmap) {
        if (bitmap != null && bitmap.isRecycled()) {
            return;
        }
        this.bitmap3 = bitmap;
        if (this.bitmap3 == null) {
            return;
        }
        runOnDraw(new Runnable() {
            public void run() {
                if (mapTextureId == OpenGlUtils.NO_TEXTURE) {
                    if (bitmap == null || bitmap.isRecycled()) {
                        return;
                    }
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
                    mapTextureId = OpenGlUtils.loadTexture(bitmap, OpenGlUtils.NO_TEXTURE, false);
                }
            }
        });
    }

    public void setOverlayTexture(final Bitmap bitmap) {
        if (bitmap != null && bitmap.isRecycled()) {
            return;
        }
        this.bitmap4 = bitmap;
        if (this.bitmap4 == null) {
            return;
        }
        runOnDraw(new Runnable() {
            public void run() {
                if (overlayTextureId == OpenGlUtils.NO_TEXTURE) {
                    if (bitmap == null || bitmap.isRecycled()) {
                        return;
                    }
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
                    overlayTextureId = OpenGlUtils.loadTexture(bitmap, OpenGlUtils.NO_TEXTURE, false);
                }
            }
        });
    }


    public void recycleBitmap() {
        if (bitmap1 != null && !bitmap1.isRecycled()) {
            bitmap1.recycle();
            bitmap1 = null;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        GLES20.glDeleteTextures(1, new int[]{
                filterSourceTexture1
        }, 0);
        filterSourceTexture1 = OpenGlUtils.NO_TEXTURE;
    }

    @Override
    protected void onDrawArraysPre() {
        GLES20.glEnableVertexAttribArray(filterSecondTextureCoordinateAttribute);
        //将纹理内容传递到片元着色器
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, filterSourceTexture1);
        GLES20.glUniform1i(filterInputTextureUniform1, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, blowoutTexturId);
        GLES20.glUniform1i(blowoutTextureUniform, 2);
//
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mapTextureId);
        GLES20.glUniform1i(mapTextureUniform, 3);
//
        GLES20.glActiveTexture(GLES20.GL_TEXTURE4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, overlayTextureId);
        GLES20.glUniform1i(overlayTextureUniform, 4);

        texture2CoordinatesBuffer.position(0);
        GLES20.glVertexAttribPointer(filterSecondTextureCoordinateAttribute, 2, GLES20.GL_FLOAT, false, 0, texture2CoordinatesBuffer);
    }

    public void setRotation(final Rotation rotation, final boolean flipHorizontal, final boolean flipVertical) {
        float[] buffer = TextureRotationUtil.getRotation(rotation, flipHorizontal, flipVertical);

        ByteBuffer bBuffer = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder());
        FloatBuffer fBuffer = bBuffer.asFloatBuffer();
        fBuffer.put(buffer);
        fBuffer.flip();

        texture2CoordinatesBuffer = bBuffer;
    }
}
