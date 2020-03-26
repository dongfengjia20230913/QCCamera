package com.jdf.camera.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jdf.camera.R;
import com.jdf.common.utils.JLog;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class BaseActivity extends AppCompatActivity {

    public final String TAG = getClass().getSimpleName();
    private static final int REQUEST_PERMISSION = 1;
    private final String[] PERMISSIONS = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();

    }

    private boolean checkPermission() {
        for (int i = 0; i < PERMISSIONS.length; i++) {
            int state = checkSelfPermission(PERMISSIONS[i]);
            JLog.d(TAG,"checkSelfPermission:"+(state==PERMISSION_GRANTED));
            if (state != PERMISSION_GRANTED) {
                requestPermissions( PERMISSIONS, REQUEST_PERMISSION);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] != PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.main_permission_hint, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_PERMISSION);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_PERMISSION) {
//            checkPermission();
        }
    }


}
