package com.jdf.camera.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.jdf.camera.R;
import com.jdf.camera.camera.loader.JCamera2OESLoader;
import com.jdf.camera.ui.CameraV2OESGLSurfaceView;
import com.jdf.common.utils.JLog;
import com.jdf.gpufilter.fiters.extend.JImageOESGrayFilter;


public class JCamera2OESFboActivity extends BaseActivity {
    String TAG = "JCamera2OESFboActivity";
    private CameraV2OESGLSurfaceView glSurfaceView;
    private JCamera2OESLoader mCameraLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2_oes);
        //获取SurfaceView
        glSurfaceView = findViewById(R.id.glsurfaceView);
        //封装相机操作
        mCameraLoader = new JCamera2OESLoader(this);
        //主要是实例化Render，并绑定
        glSurfaceView.init(mCameraLoader, false, JCamera2OESFboActivity.this);

        findViewById(R.id.switch_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                glSurfaceView.setFilter(new JImageOESGrayFilter());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean laidOut = ViewCompat.isLaidOut(glSurfaceView);
        boolean b = !glSurfaceView.isLayoutRequested();
        JLog.d(TAG, "onResume.....laidOut[%b],[%b]",laidOut,b);
        if (laidOut && b) {
            mCameraLoader.onResume(glSurfaceView.getWidth(), glSurfaceView.getHeight());
        } else {
            glSurfaceView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                           int oldRight, int oldBottom) {
                    JLog.d(TAG, "onResume.....onLayoutChange");
                    glSurfaceView.removeOnLayoutChangeListener(this);
                    mCameraLoader.onResume(glSurfaceView.getWidth(), glSurfaceView.getHeight());
                }
            });
        }
    }


}
