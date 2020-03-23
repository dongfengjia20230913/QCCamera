package com.jdf.camera.activity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.jdf.camera.R;
import com.jdf.camera.camera.CameraLoader;
import com.jdf.camera.camera.JCamera2Loader;
import com.jdf.common.utils.JLog;
import com.jdf.gpufilter.GPUImageView;
import com.jdf.gpufilter.JGPUImageRenderer;
import com.jdf.gpufilter.fiters.GPUImageFilter;
import com.jdf.gpufilter.fiters.JGPUImageFilter;
import com.jdf.gpufilter.util.Rotation;

public class Camera2Activity extends AppCompatActivity {

    GLSurfaceView glSurfaceView;
    JCamera2Loader camera2Loader;

    JGPUImageRenderer render;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        camera2Loader = new JCamera2Loader(this);
        glSurfaceView = findViewById(R.id.glsurfaceView);
        JLog.d("jiadongfeng1", "glSurfaceView: "+glSurfaceView);
        render = new JGPUImageRenderer(new JGPUImageFilter());
        glSurfaceView.setEGLContextClientVersion(2);
        updateGPUImageRotate();
        glSurfaceView.setRenderer(render);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        camera2Loader.setOnPreviewFrameListener(new CameraLoader.OnPreviewFrameListener() {
            @Override
            public void onPreviewFrame(byte[] data, int width, int height) {
                render.onPreviewFrame(data, width, height);
            }
        });

    }

    private void updateGPUImageRotate() {
        Rotation rotation = getRotation(camera2Loader.getCameraOrientation());
        boolean flipHorizontal = false;
        boolean flipVertical = false;
        if (camera2Loader.isFrontCamera()) { // 前置摄像头需要镜像
            if (rotation == Rotation.NORMAL || rotation == Rotation.ROTATION_180) {
                flipHorizontal = true;
            } else {
                flipVertical = true;
            }
        }
       render.setRotation(rotation, flipHorizontal, flipVertical);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean laidOut = ViewCompat.isLaidOut(glSurfaceView);
        boolean b = !glSurfaceView.isLayoutRequested();
        if (laidOut && b) {
            camera2Loader.onResume(glSurfaceView.getWidth(), glSurfaceView.getHeight());
        } else {
            glSurfaceView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                           int oldRight, int oldBottom) {
                    glSurfaceView.removeOnLayoutChangeListener(this);
                    camera2Loader.onResume(glSurfaceView.getWidth(), glSurfaceView.getHeight());
                }
            });
        }
    }

    private Rotation getRotation(int orientation) {
        switch (orientation) {
            case 90:
                return Rotation.ROTATION_90;
            case 180:
                return Rotation.ROTATION_180;
            case 270:
                return Rotation.ROTATION_270;
            default:
                return Rotation.NORMAL;
        }
    }
}
