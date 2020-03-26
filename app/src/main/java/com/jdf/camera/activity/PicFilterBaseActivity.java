package com.jdf.camera.activity;

import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

import com.jdf.camera.R;
import com.jdf.gpufilter.GPUImage;
import com.jdf.gpufilter.fiters.GPUImageGrayscaleFilter;


public class PicFilterBaseActivity extends BaseActivity{

    private GLSurfaceView mGLSurfaceView;
    GPUImage jgpuImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture);
        mGLSurfaceView = findViewById(R.id.surfaceView);

        //默認使用正常的濾鏡效果
        jgpuImage = new GPUImage(this);

        jgpuImage.setGLSurfaceView(mGLSurfaceView);
        jgpuImage.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.texture));

        findViewById(R.id.switch_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jgpuImage.setFilter(new GPUImageGrayscaleFilter());

            }
        });

        findViewById(R.id.switch_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jgpuImage.deleteImage();
                jgpuImage.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.texture2));

            }
        });

    }


}