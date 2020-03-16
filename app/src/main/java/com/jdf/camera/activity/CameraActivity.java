package com.jdf.camera.activity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;

import com.afei.gpuimagedemo.util.FileUtils;
import com.jdf.camera.R;
import com.jdf.camera.camera.Camera2Loader;
import com.jdf.camera.camera.CameraLoader;
import com.jdf.camera.util.ApplicationUtil;
import com.jdf.camera.util.GPUImageFilterTools;

import com.jdf.gpufilter.GPUImageView;
import com.jdf.gpufilter.fiters.GPUImageFilter;
import com.jdf.gpufilter.util.Rotation;

public class CameraActivity extends BaseActivity implements View.OnClickListener {

    private GPUImageView mGPUImageView;
    private SeekBar mSeekBar;
    private TextView mFilterNameTv;

    private GPUImageFilter mNoImageFilter = new GPUImageFilter();
    private GPUImageFilter mCurrentImageFilter = mNoImageFilter;
    private GPUImageFilterTools.FilterAdjuster mFilterAdjuster;

    private CameraLoader mCameraLoader;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initView();
        initCamera();
        ApplicationUtil.context = this;
    }

    private void initView() {
        mGPUImageView = findViewById(R.id.gpuimage);
        mSeekBar = findViewById(R.id.tone_seekbar);
        mFilterNameTv = findViewById(R.id.filter_name_tv);
        mFilterNameTv.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        findViewById(R.id.compare_iv).setOnTouchListener(mOnTouchListener);
        findViewById(R.id.close_iv).setOnClickListener(this);
        findViewById(R.id.save_iv).setOnClickListener(this);
        findViewById(R.id.switch_camera_iv).setOnClickListener(this);
    }

    private void initCamera() {
        mCameraLoader = new Camera2Loader(this);
        mCameraLoader.setOnPreviewFrameListener(new CameraLoader.OnPreviewFrameListener() {
            @Override
            public void onPreviewFrame(byte[] data, int width, int height) {
                mGPUImageView.updatePreviewFrame(data, width, height);
            }
        });
        mGPUImageView.setRatio(0.75f); // 固定使用 4:3 的尺寸
        updateGPUImageRotate();
        mGPUImageView.setRenderMode(GPUImageView.RENDERMODE_CONTINUOUSLY);
    }

    private void updateGPUImageRotate() {
        Rotation rotation = getRotation(mCameraLoader.getCameraOrientation());
        boolean flipHorizontal = false;
        boolean flipVertical = false;
        if (mCameraLoader.isFrontCamera()) { // 前置摄像头需要镜像
            if (rotation == Rotation.NORMAL || rotation == Rotation.ROTATION_180) {
                flipHorizontal = true;
            } else {
                flipVertical = true;
            }
        }
        mGPUImageView.getGPUImage().setRotation(rotation, flipHorizontal, flipVertical);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ViewCompat.isLaidOut(mGPUImageView) && !mGPUImageView.isLayoutRequested()) {
            mCameraLoader.onResume(mGPUImageView.getWidth(), mGPUImageView.getHeight());
        } else {
            mGPUImageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                           int oldRight, int oldBottom) {
                    mGPUImageView.removeOnLayoutChangeListener(this);
                    android.util.Log.d("jiadongfeng",mGPUImageView.getWidth()+"x"+mGPUImageView.getHeight());
                    mCameraLoader.onResume(mGPUImageView.getWidth(), mGPUImageView.getHeight());
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraLoader.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_name_tv:
                GPUImageFilterTools.showDialog(this, mOnGpuImageFilterChosenListener);
                break;
            case R.id.close_iv:
                finish();
                break;
            case R.id.save_iv:
                saveSnapshot();
                break;
            case R.id.switch_camera_iv:
                mGPUImageView.getGPUImage().deleteImage();
                mCameraLoader.switchCamera();
                updateGPUImageRotate();
                break;
        }
    }

    private void saveSnapshot() {
        String fileName = System.currentTimeMillis() + ".jpg";
        mGPUImageView.saveToPictures("GPUImage", fileName, mOnPictureSavedListener);
    }

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.getId() == R.id.compare_iv) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mGPUImageView.setFilter(mNoImageFilter);
                        break;
                    case MotionEvent.ACTION_UP:
                        mGPUImageView.setFilter(mCurrentImageFilter);
                        break;
                }
            }
            return true;
        }
    };

    private GPUImageFilterTools.OnGpuImageFilterChosenListener mOnGpuImageFilterChosenListener = new GPUImageFilterTools.OnGpuImageFilterChosenListener() {
        @Override
        public void onGpuImageFilterChosenListener(GPUImageFilter filter, String filterName) {
            switchFilterTo(filter);
            mFilterNameTv.setText(filterName);
        }
    };

    private void switchFilterTo(GPUImageFilter filter) {
        if (mCurrentImageFilter == null
                || (filter != null && !mCurrentImageFilter.getClass().equals(filter.getClass()))) {
            mCurrentImageFilter = filter;
            mGPUImageView.setFilter(mCurrentImageFilter);
            mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(mCurrentImageFilter);
            mSeekBar.setVisibility(mFilterAdjuster.canAdjust() ? View.VISIBLE : View.GONE);
        } else {
            mSeekBar.setVisibility(View.GONE);
        }
    }

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mFilterAdjuster != null) {
                mFilterAdjuster.adjust(progress);
            }
            mGPUImageView.requestRender();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private GPUImageView.OnPictureSavedListener mOnPictureSavedListener = new GPUImageView.OnPictureSavedListener() {
        @Override
        public void onPictureSaved(Uri uri) {
            String filePath = FileUtils.getRealFilePath(CameraActivity.this, uri);
            Log.d(TAG, "save to " + filePath);
            Toast.makeText(CameraActivity.this, "Saved: " + filePath, Toast.LENGTH_SHORT).show();
        }
    };

    private Rotation getRotation(int orientation) {
        switch (orientation) {
            case 90:
                return Rotation.ROTATION_90;
            case 180:
                return Rotation.ROTATION_180;
            case 270:
                return Rotation.ROTATION_270;
            default:
                return Rotation.NORMAL;
        }
    }
}
