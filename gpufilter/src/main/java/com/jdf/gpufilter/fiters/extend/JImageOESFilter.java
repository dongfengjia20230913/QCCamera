package com.jdf.gpufilter.fiters.extend;

import android.opengl.GLES11Ext;

import com.jdf.common.utils.ApplicaitonUtil;
import com.jdf.gpufilter.R;
import com.jdf.gpufilter.util.OpenGlUtils;
import com.jdf.gpufilter.util.ShaderUtil;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * 处理OES纹理类型数据的滤镜
 */
public class JImageOESFilter extends JGPUImageFilter {
    private static final String TAG = "JImageOESFilter";
    private int uTextureMatrixLocation = -1;
    private float[] mTransformMatrix;

    public JImageOESFilter() {
        super(ShaderUtil.loadShaderFromRes(ApplicaitonUtil.context, R.raw.base_oes_vertex_shader),
                ShaderUtil.loadShaderFromRes(ApplicaitonUtil.context, R.raw.base_oes_fragment_shader));
    }

    public JImageOESFilter(final String vertexShader, final String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public void onInit() {
        super.onInit();
        uTextureMatrixLocation = glGetUniformLocation(glProgId, "uTextureMatrix");
    }

    public void setOESTransformMatrix(float[] transformMatrix) {
        this.mTransformMatrix = transformMatrix;
    }

    public void onDrawArraysPre() {
        glUniformMatrix4fv(uTextureMatrixLocation, 1, false, mTransformMatrix, 0);
    }
    /**
     * 表示传入的纹理id为GL_TEXTURE_EXTERNAL_OES类型
     *
     * @return
     */
    public int getTextureType() {
        return GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
    }

    public int onDrawFBO(int textureId, FloatBuffer cubeBuffer, FloatBuffer textureBuffer) {
        onDraw(textureId,cubeBuffer,textureBuffer);
        return OpenGlUtils.NO_TEXTURE;
    }

}

