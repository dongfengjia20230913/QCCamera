package com.jdf.camera.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.jdf.camera.R;

import org.tensorflow.lite.examples.detection.DetectorActivity;

public class MoreActivity extends BaseActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_activity);
        initView();
    }

    private void initView() {
        findViewById(R.id.main_camera_btn).setOnClickListener(this);
        findViewById(R.id.main_gallery_btn).setOnClickListener(this);
        findViewById(R.id.tf_object).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_camera_btn:
                startActivity(new Intent(this, JCamera2OESFboActivity.class));
//                GPUImageFilterTools.showDialog(this,null);
                break;
            case R.id.main_gallery_btn:
                startActivity(new Intent(this, PicFilterBaseActivity.class));
                break;
            case R.id.tf_object:
                startActivity(new Intent(this, DetectorActivity.class));

                break;
        }
    }
}
