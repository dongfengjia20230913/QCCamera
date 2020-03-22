package com.jdf.common.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.Size;
import android.view.Surface;

public class ScreenUtil {

    public static Point getScreen(Activity context) {
        Point outSize = new Point();
        context.getWindowManager().getDefaultDisplay().getRealSize(outSize);
        return outSize;
    }

    public static int getScreenOrientation(Activity context) {
        switch (context.getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

}
