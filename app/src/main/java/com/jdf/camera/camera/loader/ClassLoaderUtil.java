package com.jdf.camera.camera.loader;

import android.util.Size;

public class ClassLoaderUtil {

    /**
     *
     * @param sizes all camera support preview sieze
     * @param width view width
     * @param height view height
     * @param aspectRatio show width and height ratio
     * @return
     */
    public static  Size getSuitableSize(Size[] sizes, int width, int height, float aspectRatio) {
        int minDelta = Integer.MAX_VALUE;
        int index = 0;

        for (int i = 0; i < sizes.length; i++) {

            Size size = sizes[i];
            // 先判断比例是否相等
            boolean isRadioSame = size.getWidth() * aspectRatio == size.getHeight();
            if (isRadioSame) {
                int delta = Math.abs(width - size.getWidth());
                if (delta == 0) {
                    return size;
                }
                if (minDelta > delta) {
                    minDelta = delta;
                    index = i;
                }
            }
        }
        return sizes[index];
    }
}
