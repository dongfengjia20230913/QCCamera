/*
 * Copyright (C) 2018 CyberAgent, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jdf.gpufilter;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

import com.jdf.gpufilter.fiters.extend.JGPUImageFilter;

public class JGPUImage {

    public enum ScaleType {CENTER_INSIDE, CENTER_CROP}


    private final Context context;
    private final JGPUImageRenderer renderer;
    private GLSurfaceView glSurfaceView;
    private GLTextureView glTextureView;
    private JGPUImageFilter filter;
    private Bitmap currentBitmap;
    private ScaleType scaleType = ScaleType.CENTER_CROP;
    private int scaleWidth, scaleHeight;

    /**
     * Instantiates a new GPUImage object.
     *
     * @param context the context
     */
    public JGPUImage(final Context context) {
        if (!supportsOpenGLES2(context)) {
            throw new IllegalStateException("OpenGL ES 2.0 is not supported on this phone.");
        }

        this.context = context;
        filter = new JGPUImageFilter();
        renderer = new JGPUImageRenderer(filter);
    }

    /**
     * Checks if OpenGL ES 2.0 is supported on the current device.
     *
     * @param context the context
     * @return true, if successful
     */
    private boolean supportsOpenGLES2(final Context context) {
        final ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }

    /**
     * Sets the GLSurfaceView which will display the preview.
     *
     * @param view the GLSurfaceView
     */
    public void setGLSurfaceView(final GLSurfaceView view) {
        glSurfaceView = view;
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        glSurfaceView.requestRender();
    }


    /**
     * Request the preview to be rendered again.
     */
    public void requestRender() {
        if (glSurfaceView != null) {
            glSurfaceView.requestRender();
        }
    }


    /**
     * Sets the filter which should be applied to the image which was (or will
     * be) set by setImage(...).
     *
     * @param filter the new filter
     */
    public void setFilter(final JGPUImageFilter filter) {
        this.filter = filter;
        renderer.setFilter(this.filter);
        requestRender();
    }

    /**
     * Sets the image on which the filter should be applied.
     *
     * @param bitmap the new image
     */
    public void setImage(final Bitmap bitmap) {
        currentBitmap = bitmap;
        renderer.setImageBitmap(bitmap, false);
        requestRender();
    }

}
