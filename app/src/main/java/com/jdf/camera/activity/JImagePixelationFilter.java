package com.jdf.camera.activity;

import android.content.Context;
import android.opengl.GLES20;

import com.jdf.gpufilter.fiters.extend.JImageOESFilter;

public class JImagePixelationFilter extends JImageOESFilter {
    public static final String PIXELATION_FRAGMENT_SHADER = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision highp float;\n" +

            "varying vec2 textureCoordinate;\n" +

            "uniform float imageWidthFactor;\n" +
            "uniform float imageHeightFactor;\n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            "uniform float pixel;\n" +

            "void main()\n" +
            "{\n" +
            "  vec2 uv  = textureCoordinate.xy;\n" +
            "  float dx = pixel * imageWidthFactor;\n" +
            "  float dy = pixel * imageHeightFactor;\n" +
            "  vec2 coord = vec2(dx * floor(uv.x / dx), dy * floor(uv.y / dy));\n" +
            "  vec3 tc = texture2D(inputImageTexture, coord).xyz;\n" +
            "  gl_FragColor = vec4(tc, 1.0);\n" +
            "}";

    private int imageWidthFactorLocation;
    private int imageHeightFactorLocation;
    private float pixel;
    private int pixelLocation;

    public JImagePixelationFilter(Context context) {
        super(NO_FILTER_VERTEX_SHADER,PIXELATION_FRAGMENT_SHADER);
        pixel = 10.9f;
    }

    @Override
    public void onInit() {
        super.onInit();
        imageWidthFactorLocation = GLES20.glGetUniformLocation(getProgram(), "imageWidthFactor");
        imageHeightFactorLocation = GLES20.glGetUniformLocation(getProgram(), "imageHeightFactor");
        pixelLocation = GLES20.glGetUniformLocation(getProgram(), "pixel");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setPixel(pixel);
    }

    @Override
    public void onOutputSizeChanged(final int width, final int height) {
        super.onOutputSizeChanged(width, height);
        setFloat(imageWidthFactorLocation, 1.0f / width);
        setFloat(imageHeightFactorLocation, 1.0f / height);
        setFloat(pixelLocation, this.pixel);

    }

    public void setPixel(final float pixel) {
        this.pixel = pixel;
        setFloat(pixelLocation, this.pixel);
    }
}
