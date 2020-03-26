package com.jdf.camera.ui;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.jdf.camera.activity.JImageOESRender;
import com.jdf.camera.camera.loader.JCamera2OESLoader;
import com.jdf.gpufilter.fiters.extend.JImageOESFboFilter;
import com.jdf.gpufilter.fiters.extend.JImageOESFilter;

/**
 * Created by lb6905 on 2017/7/19.
 */

public class CameraV2OESGLSurfaceView extends GLSurfaceView {
    public static final String TAG = "Filter_CameraV2GLSurfaceView";
    private JImageOESRender render;

    public void init(JCamera2OESLoader camera, boolean isPreviewStarted, Context context) {
        setEGLContextClientVersion(2);
        render = new JImageOESRender(new JImageOESFboFilter());
        render.init(this, camera, isPreviewStarted, context);

        setRenderer(render);
    }

    public CameraV2OESGLSurfaceView(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    public void setFilter(JImageOESFilter filte){
        render.setFilter(filte);
    }
}
