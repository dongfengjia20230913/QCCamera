package com.jdf.camera.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.jdf.camera.camera.Camera2OESLoader;
import com.jdf.camera.ui.CameraV2GLSurfaceView;
import com.jdf.common.utils.JLog;

/**
 * Created by lb6905 on 2017/7/19.
 */

public class Camera2OESActivity extends Activity {
    private CameraV2GLSurfaceView mCameraV2GLSurfaceView;
    private Camera2OESLoader mCameraLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCameraV2GLSurfaceView = new CameraV2GLSurfaceView(this);
        setContentView(mCameraV2GLSurfaceView);
        mCameraLoader = new Camera2OESLoader(this);
        mCameraV2GLSurfaceView.init(mCameraLoader, false, Camera2OESActivity.this);
    }

    @Override
    protected void onResume() {
        JLog.d("jiadongfeng5", "onResume.....");

        super.onResume();
        boolean laidOut = ViewCompat.isLaidOut(mCameraV2GLSurfaceView);
        boolean b = !mCameraV2GLSurfaceView.isLayoutRequested();
        if (laidOut && b) {
            mCameraLoader.onResume(mCameraV2GLSurfaceView.getWidth(), mCameraV2GLSurfaceView.getHeight());
        } else {
            mCameraV2GLSurfaceView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                           int oldRight, int oldBottom) {
                    mCameraV2GLSurfaceView.removeOnLayoutChangeListener(this);
                    mCameraLoader.onResume(mCameraV2GLSurfaceView.getWidth(), mCameraV2GLSurfaceView.getHeight());
                }
            });
        }
    }

}
