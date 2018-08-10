package com.ping.android.device.impl;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;

import com.ping.android.device.Device;
import com.ping.android.utils.Log;

import java.io.File;

import javax.inject.Inject;

/**
 * Created by tuanluong on 2/6/18.
 */

public class DeviceImpl implements Device {
    @Inject
    Application application;

    @Inject
    public DeviceImpl() {
    }

    @SuppressLint("HardwareIds")
    @Override
    public String getDeviceId() {
        return Settings.Secure.getString(application.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    @Override
    public boolean getNetworkStatus() {
        ConnectivityManager cm = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public String getExternalImageFolder() {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File bzzzFolder = new File(root, "bzzz");
        if (!bzzzFolder.exists()) {
            bzzzFolder.mkdirs();
        }
        return bzzzFolder.getAbsolutePath();
    }

    @Override
    public void refreshMedia(String file) {
        MediaScannerConnection.scanFile(application, new String[]{file}, null, (path, uri) -> {
            Log.i("Scanned " + path + ":");
            Log.i("uri=" + uri);
        });
    }


}
