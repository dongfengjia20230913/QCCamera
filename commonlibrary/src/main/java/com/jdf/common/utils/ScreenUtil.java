package com.jdf.common.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.Size;

public class ScreenUtil {

    public static Point getScreen(Activity context) {
        Point outSize = new Point();
        context.getWindowManager().getDefaultDisplay().getRealSize(outSize);
        return outSize;
    }

}
