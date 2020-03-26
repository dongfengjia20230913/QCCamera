package com.jdf.camera.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.jdf.camera.R;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        findViewById(R.id.main_camera_btn).setOnClickListener(this);
        findViewById(R.id.main_gallery_btn).setOnClickListener(this);
        findViewById(R.id.more).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_camera_btn:
                startActivity(new Intent(this, CameraActivity.class));
//                GPUImageFilterTools.showDialog(this,null);
                break;
            case R.id.main_gallery_btn:
                startActivity(new Intent(this, GalleryActivity.class));
                break;
            case R.id.more:
                startActivity(new Intent(this, MoreActivity.class));

                break;
        }
    }
}
