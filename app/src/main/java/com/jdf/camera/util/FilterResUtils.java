package com.jdf.camera.util;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FilterResUtils {

    //从sh脚本中加载shader内容的方法
    public static String loadShaderFromAssetFilter(String shaderName) {
        String result = null;
        try {
            InputStream in = ApplicationUtil.context.getClass().getClassLoader().getResourceAsStream("assets/filer/" + shaderName);
            int ch = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((ch = in.read()) != -1) {
                baos.write(ch);
            }
            byte[] buff = baos.toByteArray();
            baos.close();
            in.close();
            result = new String(buff, "UTF-8");
            result = result.replaceAll("\\r\\n", "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //从sh脚本中加载shader内容的方法
    public static Bitmap loadBitmapFromAssetFilter(String textName) {
        Bitmap result = null;
        InputStream in = null;
        try {
            in = ApplicationUtil.context.getClass().getClassLoader().getResourceAsStream("assets/filter/" + textName);
            if(in!=null) {
                result = BitmapFactory.decodeStream(in);
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


    private Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = ApplicationUtil.context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;

    }
}
