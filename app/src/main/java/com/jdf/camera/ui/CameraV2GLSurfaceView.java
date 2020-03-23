package com.jdf.camera.ui;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.jdf.camera.camera.Camera2OESLoader;
import com.jdf.camera.activity.CameraV2Renderer;

/**
 * Created by lb6905 on 2017/7/19.
 */

public class CameraV2GLSurfaceView extends GLSurfaceView {
    public static final String TAG = "Filter_CameraV2GLSurfaceView";
    private CameraV2Renderer mCameraV2Renderer;

    public void init(Camera2OESLoader camera, boolean isPreviewStarted, Context context) {
        setEGLContextClientVersion(2);

        mCameraV2Renderer = new CameraV2Renderer();
        mCameraV2Renderer.init(this, camera, isPreviewStarted, context);

        setRenderer(mCameraV2Renderer);
    }

    public CameraV2GLSurfaceView(Context context) {
        super(context);
    }
}
