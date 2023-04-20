package com.filenko.conspectnote.common;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

public class PermissionsHelper {
    public static final int PERMISSION_REQUEST_CODE = 123;

    public static void checkStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            // Для устройств до Android 11 запрашиваем разрешение на запись во внешнее хранилище
            if (ContextCompat
                    .checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        } else {
            // Для устройств Android 11 и выше используем Scoped Storage
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivityForResult(intent, PERMISSION_REQUEST_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean isStoragePermissionGranted(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                // Для устройств до Android 11 проверяем разрешение на запись во внешнее хранилище
                return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            } else {
                // Для устройств Android 11 и выше проверяем, есть ли разрешение на работу со всеми файлами
                return Environment.isExternalStorageManager();
            }
        }
        return false;
    }

    public static void checkResultPermissionsChoice(Activity activity,
                                                    int requestCode,
                                                    String[] permissions,
                                                    int[] grantResults) {
        if (requestCode == PermissionsHelper.PERMISSION_REQUEST_CODE) {
            if (!PermissionsHelper
                    .isStoragePermissionGranted(requestCode, permissions, grantResults)) {
                activity.finish();
            }
        }
    }
}