package com.jdf.gpufilter.fiters.extend;

import android.opengl.GLES20;

import com.jdf.common.utils.ApplicaitonUtil;
import com.jdf.gpufilter.R;
import com.jdf.gpufilter.util.OpenGlUtils;
import com.jdf.gpufilter.util.ShaderUtil;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * 支持FBO的OES类型滤镜
 */
public class JImageOESFboFilter extends JImageOESFilter {
    private static final String TAG = "JImageOESFboFilter";

    //FBO对象引用
    private int[] frameBuffers = new int[1];
    //与FBO对象绑定的纹理id
    public int[] frameBufferTextures = new int[1];

    public JImageOESFboFilter() {
        super(ShaderUtil.loadShaderFromRes(ApplicaitonUtil.context, R.raw.base_oes_vertex_shader),
                ShaderUtil.loadShaderFromRes(ApplicaitonUtil.context, R.raw.base_oes_fragment_shader));
    }

    public JImageOESFboFilter(final String vertexShader, final String fragmentShader) {
        super(vertexShader, fragmentShader);
    }



    public void onOutputSizeChanged(final int width, final int height) {
        super.onOutputSizeChanged(width, height);
        //当surface有变化时，重新生成FBO
        if (frameBuffers != null) {
            destroyFramebuffers();
        }
        //创建FBO对象
        OpenGlUtils.createFrameBuffer(frameBuffers, frameBufferTextures, width, height);
    }


    public int onDrawFBO(int textureId, FloatBuffer cubeBuffer, FloatBuffer textureBuffer) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0]);
        onDraw(textureId, cubeBuffer, textureBuffer);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return frameBufferTextures[0];
    }

    private void destroyFramebuffers() {
        if (frameBufferTextures != null) {
            GLES20.glDeleteTextures(frameBufferTextures.length, frameBufferTextures, 0);
        }
        if (frameBuffers != null) {
            GLES20.glDeleteFramebuffers(frameBuffers.length, frameBuffers, 0);
        }
    }


}

