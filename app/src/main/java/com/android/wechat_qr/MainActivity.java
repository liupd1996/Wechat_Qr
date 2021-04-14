package com.android.wechat_qr;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.wechat_qrcode.WeChatQRCode;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    WeChatQRCode weChatQRCode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

    private void initView() {
        findViewById(R.id.btn_scan).setOnClickListener(v -> {
            if (checkPermission()) {
                Intent intent = new Intent(MainActivity.this,WechatScanActivity.class);
                startActivity(intent);
            }
        });
    }

    public String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};

    @TargetApi(23)
    private boolean checkPermission() {
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 1);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;
        if (1 == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }

            if (hasPermissionDismiss) {
                //
            } else {
                Intent intent = new Intent(MainActivity.this,WechatScanActivity.class);
                startActivity(intent);
            }
        }
    }

}