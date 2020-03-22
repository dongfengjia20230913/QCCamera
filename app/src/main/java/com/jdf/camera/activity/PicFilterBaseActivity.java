package com.jdf.camera.activity;

import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.jdf.camera.R;
import com.jdf.gpufilter.JGPUImageRenderer;
import com.jdf.gpufilter.fiters.JGPUImageFilter;


public class PicFilterBaseActivity extends BaseActivity{

    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture);
        mGLSurfaceView = findViewById(R.id.surfaceView);

        JGPUImageFilter normalFilter = new JGPUImageFilter();
        JGPUImageRenderer renderer = new JGPUImageRenderer(normalFilter);
        setGLSurfaceViewRender(renderer);
        renderer.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.texture));


    }

    public void setGLSurfaceViewRender(JGPUImageRenderer renderer) {
        //从布局文件中，获取GLSurfaceView对象
        mGLSurfaceView.setEGLContextClientVersion(2);
        //设置颜色缓存为RGBA,位数为8888
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        //设置GLSurfaceView的render
        mGLSurfaceView.setRenderer(renderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        //请求刷新GLSurfaceView
        mGLSurfaceView.requestRender();
    }

}