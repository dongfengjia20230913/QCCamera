package com.jdf.camera.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Environment;

import com.jdf.common.utils.JLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    public static byte[] generateNV21Data(Image image) {
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = width * height + 1;
                    outputStride = 2;
                    break;
                case 2:
                    channelOffset = width * height;
                    outputStride = 2;
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            int shift = i == 0 ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }


    public static boolean   saveBitmap = false;
    public static Bitmap ImageToBitmap(Image image) {
        if (image == null) return null;
        ByteArrayOutputStream outputbytes = new ByteArrayOutputStream();

        ByteBuffer bufferY = image.getPlanes()[0].getBuffer();
        byte[] data0 = new byte[bufferY.remaining()];
        bufferY.get(data0);

        ByteBuffer bufferU = image.getPlanes()[1].getBuffer();
        byte[] data1 = new byte[bufferU.remaining()];
        bufferU.get(data1);

        ByteBuffer bufferV = image.getPlanes()[2].getBuffer();
        byte[] data2 = new byte[bufferV.remaining()];
        bufferV.get(data2);

        try {
            outputbytes.write(data0);
            outputbytes.write(data2);
            outputbytes.write(data1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final YuvImage yuvImage = new YuvImage(outputbytes.toByteArray(), ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream outBitmap = new ByteArrayOutputStream();

        yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 95, outBitmap);
        return BitmapFactory.decodeByteArray(outBitmap.toByteArray(), 0, outBitmap.size());
    }

    public static void saveBitmap(final Bitmap bitmap) {
        saveBitmap(bitmap, "preview.png");
    }

    /**
     * Saves a Bitmap object to disk for analysis.
     *
     * @param bitmap The bitmap to save.
     * @param filename The location to save the bitmap to.
     */
    public static void saveBitmap(final Bitmap bitmap, final String filename) {
        final String root =
                Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "tensorflow";
        JLog.i(TAG,"Saving %dx%d bitmap to %s.", bitmap.getWidth(), bitmap.getHeight(), root);
        final File myDir = new File(root);

        if (!myDir.mkdirs()) {
            JLog.i(TAG,"Make dir failed");
        }

        final String fname = filename;
        final File file = new File(myDir, fname);
        if (file.exists()) {
            file.delete();
        }
        try {
            final FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 99, out);
            out.flush();
            out.close();
        } catch (final Exception e) {
            JLog.e(TAG,e+ "Exception!");
        }
    }
}
