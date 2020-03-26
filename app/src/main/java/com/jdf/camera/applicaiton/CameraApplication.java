package com.jdf.camera.applicaiton;

import android.app.Application;
import android.content.Context;

import com.jdf.common.utils.ApplicaitonUtil;

public class CameraApplication extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicaitonUtil.context = getApplicationContext();
    }
}
